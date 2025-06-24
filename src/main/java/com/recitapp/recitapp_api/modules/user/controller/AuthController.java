package com.recitapp.recitapp_api.modules.user.controller;

import com.recitapp.recitapp_api.modules.user.dto.*;
import com.recitapp.recitapp_api.modules.user.service.AuthService;
import com.recitapp.recitapp_api.modules.user.service.GuestUserService;
import com.recitapp.recitapp_api.modules.user.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para autenticación y autorización")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final GuestUserService guestUserService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Credenciales inválidas", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message", e.getMessage()));
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validar token", description = "Valida si un token JWT es válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Token requerido"));
            }

            boolean isValid = authService.validateToken(token);
            if (isValid) {
                return ResponseEntity.ok(Map.of("valid", true, "message", "Token válido"));
            } else {
                return ResponseEntity.status(401)
                        .body(Map.of("valid", false, "message", "Token inválido"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida el refresh token del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout exitoso")
    })
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Refresh token requerido"));
            }

            authService.logout(refreshToken);
            return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error durante el logout", "message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Email o DNI ya registrado")
    })
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest registrationRequest, 
                                    HttpServletRequest request) {
        // Register endpoint called
        try {
            // Verificar si es un usuario invitado
            boolean isGuest = guestUserService.isGuestRequest(request);
            GuestUserService.GuestUserInfo guestInfo = null;
            
            if (isGuest) {
                guestInfo = guestUserService.getGuestUserInfo(request);
            }

            AuthResponse authResponse = authService.register(registrationRequest);
            
            // Agregar información adicional en la respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("token", authResponse.getToken());
            response.put("type", authResponse.getType());
            response.put("refreshToken", authResponse.getRefreshToken());
            response.put("userId", authResponse.getUserId());
            response.put("email", authResponse.getEmail());
            response.put("firstName", authResponse.getFirstName());
            response.put("lastName", authResponse.getLastName());
            response.put("role", authResponse.getRole());
            response.put("expiresIn", authResponse.getExpiresIn());
            response.put("wasGuest", isGuest);
            
            if (guestInfo != null) {
                Map<String, Object> guestInfoMap = new HashMap<>();
                guestInfoMap.put("ipAddress", guestInfo.getIpAddress());
                guestInfoMap.put("sessionId", guestInfo.getSessionId());
                response.put("guestInfo", guestInfoMap);
            } else {
                response.put("guestInfo", null);
            }
            
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(409)
                    .body(Map.of("error", "Error de registro", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Genera un nuevo access token usando el refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    })
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        try {
            AuthResponse authResponse = authService.refreshToken(refreshRequest.getRefreshToken());
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Token inválido", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Envía un email con enlace para restablecer contraseña")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email de recuperación enviado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos")
    })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordResetRequest resetRequest) {
        log.info("=== FORGOT PASSWORD REQUEST RECEIVED ===");
        log.info("Email solicitado: {}", resetRequest.getEmail());
        
        try {
            log.info("Llamando a passwordResetService.initiatePasswordReset()...");
            boolean isTemporaryAccountCreated = passwordResetService.initiatePasswordReset(resetRequest.getEmail());
            log.info("✅ passwordResetService.initiatePasswordReset() completado exitosamente");
            
            if (isTemporaryAccountCreated) {
                return ResponseEntity.ok(Map.of(
                        "message", "Hemos creado una cuenta temporal para tu email. Te hemos enviado las credenciales por email para que puedas iniciar sesión.",
                        "isTemporaryAccount", true
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "message", "Si el email existe en nuestro sistema, recibirás un enlace de recuperación.",
                        "isTemporaryAccount", false
                ));
            }
        } catch (RuntimeException e) {
            log.error("❌ RuntimeException en forgotPassword: {}", e.getMessage(), e);
            // Por seguridad, no revelamos si el email existe o no
            if (e.getMessage().contains("Demasiados intentos")) {
                return ResponseEntity.status(429)
                        .body(Map.of("error", "Demasiados intentos", "message", e.getMessage()));
            }
            return ResponseEntity.ok(Map.of(
                    "message", "Si el email existe en nuestro sistema, recibirás un enlace de recuperación.",
                    "isTemporaryAccount", false
            ));
        } catch (Exception e) {
            log.error("❌ Exception en forgotPassword: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña", description = "Restablece la contraseña usando el token de recuperación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido o contraseñas no coinciden"),
            @ApiResponse(responseCode = "410", description = "Token expirado")
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest resetConfirmRequest) {
        try {
            // Validar que las contraseñas coincidan
            if (!resetConfirmRequest.getNewPassword().equals(resetConfirmRequest.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Las contraseñas no coinciden"));
            }

            passwordResetService.resetPassword(resetConfirmRequest.getToken(), resetConfirmRequest.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña restablecida exitosamente"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("expirado")) {
                return ResponseEntity.status(410)
                        .body(Map.of("error", "Token expirado", "message", e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token inválido", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message", e.getMessage()));
        }
    }

    @GetMapping("/validate-reset-token")
    @Operation(summary = "Validar token de recuperación", description = "Valida si un token de recuperación es válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "400", description = "Token inválido")
    })
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        try {
            boolean isValid = passwordResetService.validateResetToken(token);
            if (isValid) {
                return ResponseEntity.ok(Map.of("valid", true, "message", "Token válido"));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("valid", false, "message", "Token inválido o expirado"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message", e.getMessage()));
        }
    }

    @GetMapping("/guest-status")
    @Operation(summary = "Verificar estado de usuario invitado", description = "Verifica si el usuario actual es un invitado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado verificado exitosamente")
    })
    public ResponseEntity<?> getGuestStatus(HttpServletRequest request) {
        try {
            boolean isGuest = guestUserService.isGuestRequest(request);
            GuestUserService.GuestUserInfo guestInfo = null;
            
            if (isGuest) {
                guestInfo = guestUserService.getGuestUserInfo(request);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("isGuest", isGuest);
            response.put("isAuthenticated", !isGuest);
            response.put("canRegister", isGuest);
            
            if (guestInfo != null) {
                Map<String, Object> guestInfoMap = new HashMap<>();
                guestInfoMap.put("ipAddress", guestInfo.getIpAddress());
                guestInfoMap.put("userAgent", guestInfo.getUserAgent());
                guestInfoMap.put("sessionId", guestInfo.getSessionId());
                response.put("guestInfo", guestInfoMap);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message", e.getMessage()));
        }
    }
} 