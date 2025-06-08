package com.recitapp.recitapp_api.modules.notification.controller;

import com.recitapp.recitapp_api.modules.notification.entity.UserDeviceToken;
import com.recitapp.recitapp_api.modules.notification.service.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping("/register")
    public ResponseEntity<UserDeviceToken> registerDeviceToken(
            @RequestParam Long userId,
            @RequestParam String deviceToken,
            @RequestParam UserDeviceToken.DeviceType deviceType,
            @RequestParam(required = false) String deviceName) {
        
        UserDeviceToken token = deviceTokenService.registerDeviceToken(userId, deviceToken, deviceType, deviceName);
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<UserDeviceToken> updateDeviceToken(
            @RequestParam Long userId,
            @RequestParam String oldToken,
            @RequestParam String newToken) {
        
        UserDeviceToken updatedToken = deviceTokenService.updateDeviceToken(userId, oldToken, newToken);
        return ResponseEntity.ok(updatedToken);
    }

    @DeleteMapping("/deactivate")
    public ResponseEntity<Void> deactivateDeviceToken(@RequestParam String deviceToken) {
        deviceTokenService.deactivateDeviceToken(deviceToken);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}/deactivate-all")
    public ResponseEntity<Void> deactivateAllUserTokens(@PathVariable Long userId) {
        deviceTokenService.deactivateAllUserTokens(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserDeviceToken>> getUserActiveTokens(@PathVariable Long userId) {
        List<UserDeviceToken> tokens = deviceTokenService.getUserActiveTokens(userId);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<String> getLatestUserToken(@PathVariable Long userId) {
        String token = deviceTokenService.getLatestUserToken(userId);
        if (token != null) {
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String deviceToken) {
        boolean isValid = deviceTokenService.isTokenValid(deviceToken);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupOldTokens() {
        try {
            deviceTokenService.cleanupOldTokens();
            return ResponseEntity.ok("Limpieza de tokens completada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error durante la limpieza: " + e.getMessage());
        }
    }

    // Endpoint de utilidad para registrar token desde el front-end
    @PostMapping("/register-from-app")
    public ResponseEntity<String> registerTokenFromApp(@RequestBody RegisterTokenRequest request) {
        try {
            deviceTokenService.registerDeviceToken(
                request.getUserId(), 
                request.getDeviceToken(), 
                request.getDeviceType(), 
                request.getDeviceName()
            );
            return ResponseEntity.ok("Token registrado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error registrando token: " + e.getMessage());
        }
    }

    // DTO para el registro desde la app
    public static class RegisterTokenRequest {
        private Long userId;
        private String deviceToken;
        private UserDeviceToken.DeviceType deviceType;
        private String deviceName;

        // Getters y setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getDeviceToken() { return deviceToken; }
        public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
        
        public UserDeviceToken.DeviceType getDeviceType() { return deviceType; }
        public void setDeviceType(UserDeviceToken.DeviceType deviceType) { this.deviceType = deviceType; }
        
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    }
} 