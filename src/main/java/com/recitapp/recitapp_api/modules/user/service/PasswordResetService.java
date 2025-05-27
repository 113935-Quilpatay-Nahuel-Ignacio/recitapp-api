package com.recitapp.recitapp_api.modules.user.service;

import com.recitapp.recitapp_api.modules.user.entity.PasswordResetToken;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.PasswordResetTokenRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
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

    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        // Verificar límite de intentos
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentAttempts = passwordResetTokenRepository.countRecentTokensByUser(user, oneHourAgo);
        
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

        // Enviar email
        sendPasswordResetEmail(user, resetToken.getToken());
    }

    private void sendPasswordResetEmail(User user, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Recuperación de Contraseña - RecitApp");
            
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
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
            mailSender.send(message);
            
            log.info("Email de recuperación enviado a: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error enviando email de recuperación a {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Error enviando email de recuperación");
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
} 