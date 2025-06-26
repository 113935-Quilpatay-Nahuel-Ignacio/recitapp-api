package com.recitapp.recitapp_api.modules.user.service;

import com.recitapp.recitapp_api.config.JwtService;
import com.recitapp.recitapp_api.modules.user.dto.AuthResponse;
import com.recitapp.recitapp_api.modules.user.dto.LoginRequest;
import com.recitapp.recitapp_api.modules.user.dto.UserRegistrationRequest;
import com.recitapp.recitapp_api.modules.user.entity.RefreshToken;
import com.recitapp.recitapp_api.modules.user.entity.Role;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.RoleRepository;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration:86400000}") // 24 horas por defecto
    private long jwtExpiration;

    @Value("${jwt.expiration.remember:2592000000}") // 30 días para remember me
    private long jwtExpirationRemember;

    public AuthResponse login(LoginRequest request) {
        try {
            // Autenticar al usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Obtener detalles del usuario
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userDetailsService.getUserByEmail(userDetails.getUsername());

            // Actualizar última conexión
            user.setLastConnection(LocalDateTime.now());
            userRepository.save(user);

            // Determinar duración del token basado en rememberMe
            long tokenExpiration = request.isRememberMe() ? jwtExpirationRemember : jwtExpiration;
            
            // Generar token JWT con duración apropiada
            String token = jwtService.generateTokenWithExpiration(userDetails, tokenExpiration);

            // Generar refresh token con duración extendida si rememberMe es true
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, request.isRememberMe());

            // Construir respuesta
            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .refreshToken(refreshToken.getToken())
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole().getName())
                    .expiresIn(tokenExpiration)
                    .refreshExpiresIn(refreshTokenService.getRefreshTokenExpiration())
                    .build();

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales inválidas");
        } catch (Exception e) {
            throw new RuntimeException("Error durante la autenticación: " + e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtService.isTokenValid(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token no encontrado"));

        refreshTokenService.verifyExpiration(refreshToken);

        if (!refreshTokenService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Refresh token inválido");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        // Generar nuevo access token
        String newAccessToken = jwtService.generateToken(userDetails);

        // Generar nuevo refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .token(newAccessToken)
                .type("Bearer")
                .refreshToken(newRefreshToken.getToken())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().getName())
                .expiresIn(jwtExpiration)
                .refreshExpiresIn(refreshTokenService.getRefreshTokenExpiration())
                .build();
    }

    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        // Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }

        // Verificar que el email no esté en uso
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Verificar que el DNI no esté en uso
        if (userRepository.findByDni(request.getDni()).isPresent()) {
            throw new RuntimeException("El DNI ya está registrado");
        }

        // Determinar el rol basado en el dominio del email (ASIGNACIÓN AUTOMÁTICA)
        Role userRole;
        String email = request.getEmail().toLowerCase(); // Normalizar a minúsculas
        
        if (email.endsWith("@recitapp-admin.com")) {
            userRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
            log.info("🔐 ASIGNACIÓN AUTOMÁTICA: Rol ADMIN asignado al email: {}", request.getEmail());
        } else if (email.endsWith("@recitapp-moderator.com")) {
            userRole = roleRepository.findByName("MODERADOR")
                    .orElseThrow(() -> new RuntimeException("Rol MODERADOR no encontrado"));
            log.info("🛡️ ASIGNACIÓN AUTOMÁTICA: Rol MODERADOR asignado al email: {}", request.getEmail());
        } else if (email.endsWith("@recitapp-verifier.com")) {
            userRole = roleRepository.findByName("REGISTRADOR_EVENTO")
                    .orElseThrow(() -> new RuntimeException("Rol REGISTRADOR_EVENTO no encontrado"));
            log.info("📝 ASIGNACIÓN AUTOMÁTICA: Rol REGISTRADOR_EVENTO asignado al email: {}", request.getEmail());
        } else if (email.endsWith("@recitapp-ticket-validator.com")) {
            userRole = roleRepository.findByName("VERIFICADOR_ENTRADAS")
                    .orElseThrow(() -> new RuntimeException("Rol VERIFICADOR_ENTRADAS no encontrado"));
            log.info("🎫 ASIGNACIÓN AUTOMÁTICA: Rol VERIFICADOR_ENTRADAS asignado al email: {}", request.getEmail());
        } else {
            userRole = roleRepository.findByName("COMPRADOR")
                    .orElseThrow(() -> new RuntimeException("Rol COMPRADOR no encontrado"));
            log.info("👤 Rol por defecto COMPRADOR asignado al email: {}", request.getEmail());
        }

        // Crear nuevo usuario
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dni(request.getDni())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(userRole)
                .active(true)
                .authMethod("EMAIL")
                .walletBalance(0.0)
                .registrationDate(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        log.info("✅ Usuario registrado exitosamente: {} con rol: {} (ID: {})", 
                user.getEmail(), user.getRole().getName(), user.getRole().getId());

        // Generar tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .token(accessToken)
                .type("Bearer")
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().getName())
                .expiresIn(jwtExpiration)
                .refreshExpiresIn(refreshTokenService.getRefreshTokenExpiration())
                .build();
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token no encontrado"));
        
        refreshTokenService.revokeToken(refreshToken);
    }
} 