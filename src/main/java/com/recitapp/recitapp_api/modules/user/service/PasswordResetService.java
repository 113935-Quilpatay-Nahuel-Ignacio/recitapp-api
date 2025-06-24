package com.recitapp.recitapp_api.modules.user.service;

import com.recitapp.recitapp_api.modules.user.entity.PasswordResetToken;
import com.recitapp.recitapp_api.modules.user.entity.Role;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.PasswordResetTokenRepository;
import com.recitapp.recitapp_api.modules.user.repository.RoleRepository;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.expiration:3600000}") // 1 hora por defecto
    private long passwordResetExpiration;

    @Value("${app.password-reset.max-attempts:3}") // Máximo 3 intentos por hora
    private int maxResetAttempts;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${spring.mail.host}")
    private String mailHost;
    
    @Value("${spring.mail.port}")
    private String mailPort;

    @Transactional
    public boolean initiatePasswordReset(String email) {
        log.info("Iniciando proceso de recuperación de contraseña para email: {}", email);
        
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            log.info("Usuario no encontrado con email: {}. Creando cuenta temporal...", email);
            user = createTemporaryUserAccount(email);
            log.info("Cuenta temporal creada para: {} con ID: {}", email, user.getId());
            // Para usuarios temporales, no enviamos link de reset, ya enviamos las credenciales
            return true; // Retornamos true para indicar que se creó una cuenta temporal
        } else {
            log.info("Usuario existente encontrado: {} {}", user.getFirstName(), user.getLastName());
        }

        // Verificar límite de intentos
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentAttempts = passwordResetTokenRepository.countRecentTokensByUser(user, oneHourAgo);
        
        log.info("Intentos recientes en la última hora: {}", recentAttempts);
        
        if (recentAttempts >= maxResetAttempts) {
            throw new RuntimeException("Demasiados intentos de recuperación. Intenta nuevamente en una hora.");
        }

        // Invalidar tokens existentes
        passwordResetTokenRepository.invalidateAllUserTokens(user, LocalDateTime.now());

        // Crear nuevo token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(passwordResetExpiration / 1000))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);
        log.info("Token de recuperación creado y guardado: {}", resetToken.getToken());

        // Enviar email
        try {
            sendPasswordResetEmail(user, resetToken.getToken());
            log.info("Proceso de recuperación completado exitosamente para: {}", email);
        } catch (Exception e) {
            log.error("Error durante el envío de email de recuperación: {}", e.getMessage(), e);
            throw e; // Re-lanzar la excepción
        }
        
        return false; // Retornamos false para indicar que es un usuario existente
    }

    private void sendPasswordResetEmail(User user, String token) {
        try {
            log.info("Preparando email de recuperación para: {}", user.getEmail());
            log.info("Email configurado desde: {}", fromEmail);
            log.info("Frontend URL configurado: {}", frontendUrl);
            log.info("Mail host configurado: {}", mailHost);
            log.info("Mail port configurado: {}", mailPort);
            
            // Verificar configuración de JavaMailSender
            log.info("JavaMailSender instance: {}", mailSender != null ? "OK" : "NULL");
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Recuperación de Contraseña - RecitApp");
            
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            log.info("URL de reset generada: {}", resetUrl);
            
            String emailContent = String.format(
                "Hola %s,\n\n" +
                "Has solicitado restablecer tu contraseña en RecitApp.\n\n" +
                "Haz clic en el siguiente enlace para crear una nueva contraseña:\n" +
                "%s\n\n" +
                "Este enlace expirará en 1 hora.\n\n" +
                "Si no solicitaste este cambio, puedes ignorar este email.\n\n" +
                "Saludos,\n" +
                "El equipo de RecitApp",
                user.getFirstName(),
                resetUrl
            );
            
            message.setText(emailContent);
            
            log.info("Enviando email usando JavaMailSender...");
            mailSender.send(message);
            
            log.info("✅ Email de recuperación enviado exitosamente a: {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Error enviando email de recuperación a {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error enviando email de recuperación: " + e.getMessage());
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new RuntimeException("Token de recuperación inválido o ya utilizado"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("Token de recuperación expirado");
        }

        if (resetToken.isUsed()) {
            throw new RuntimeException("Token de recuperación ya utilizado");
        }

        // Actualizar contraseña del usuario
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Marcar token como usado
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        log.info("Contraseña restablecida exitosamente para usuario: {}", user.getEmail());
    }

    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token);
        return resetToken.isPresent() && !resetToken.get().isExpired();
    }

    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
    
    private User createTemporaryUserAccount(String email) {
        log.info("Creando cuenta temporal para email: {}", email);
        
        // Generar contraseña temporal segura
        String temporaryPassword = generateSecurePassword();
        log.info("Contraseña temporal generada para: {}", email);
        
        // Obtener rol de COMPRADOR por defecto
        Role compradorRole = roleRepository.findByName("COMPRADOR")
                .orElseThrow(() -> new RuntimeException("Rol COMPRADOR no encontrado"));
        
        // Crear usuario temporal
        User tempUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(temporaryPassword))
                .firstName("Usuario")
                .lastName("Temporal")
                .dni("00000000") // DNI temporal
                .role(compradorRole)
                .build();
        
        // Guardar usuario
        User savedUser = userRepository.save(tempUser);
        log.info("Usuario temporal guardado con ID: {}", savedUser.getId());
        
        // Enviar credenciales por email
        sendTemporaryCredentialsEmail(savedUser, temporaryPassword);
        
        return savedUser;
    }
    
    private String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Generar contraseña de 12 caracteres
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    private void sendTemporaryCredentialsEmail(User user, String temporaryPassword) {
        try {
            log.info("Enviando credenciales temporales a: {}", user.getEmail());
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Cuenta Temporal Creada - RecitApp");
            
            String emailContent = String.format(
                "Hola,\n\n" +
                "Hemos creado una cuenta temporal para ti en RecitApp.\n\n" +
                "Tus credenciales temporales son:\n" +
                "Email: %s\n" +
                "Contraseña temporal: %s\n\n" +
                "IMPORTANTE: Esta es una contraseña temporal. Por seguridad, te recomendamos:\n" +
                "1. Iniciar sesión con estas credenciales\n" +
                "2. Ir a tu perfil y cambiar la contraseña por una más memorable\n" +
                "3. Completar tu información personal\n\n" +
                "Si no solicitaste esta cuenta, puedes ignorar este email.\n\n" +
                "Saludos,\n" +
                "El equipo de RecitApp",
                user.getEmail(),
                temporaryPassword
            );
            
            message.setText(emailContent);
            mailSender.send(message);
            
            log.info("✅ Credenciales temporales enviadas a: {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Error enviando credenciales temporales a {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error enviando credenciales temporales: " + e.getMessage());
        }
    }
} 