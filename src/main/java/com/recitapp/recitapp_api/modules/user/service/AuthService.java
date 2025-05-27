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

            // Generar token JWT
            String token = jwtService.generateToken(userDetails);

            // Generar refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

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
                    .expiresIn(jwtExpiration)
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

        // Obtener rol por defecto (COMPRADOR)
        Role defaultRole = roleRepository.findByName("COMPRADOR")
                .orElseThrow(() -> new RuntimeException("Rol por defecto no encontrado"));

        // Crear nuevo usuario
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dni(request.getDni())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(defaultRole)
                .active(true)
                .authMethod("EMAIL")
                .walletBalance(0.0)
                .registrationDate(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

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