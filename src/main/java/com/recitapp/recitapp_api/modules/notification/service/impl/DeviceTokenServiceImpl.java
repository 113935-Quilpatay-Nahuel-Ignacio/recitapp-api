package com.recitapp.recitapp_api.modules.notification.service.impl;

import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import com.recitapp.recitapp_api.modules.notification.entity.UserDeviceToken;
import com.recitapp.recitapp_api.modules.notification.repository.UserDeviceTokenRepository;
import com.recitapp.recitapp_api.modules.notification.service.DeviceTokenService;
import com.recitapp.recitapp_api.modules.notification.service.PushNotificationService;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final UserDeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    @Override
    @Transactional
    public UserDeviceToken registerDeviceToken(Long userId, String deviceToken, 
                                             UserDeviceToken.DeviceType deviceType, String deviceName) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));

        // Verificar si el token ya existe para este usuario
        Optional<UserDeviceToken> existingToken = deviceTokenRepository
            .findByUserIdAndDeviceToken(userId, deviceToken);

        if (existingToken.isPresent()) {
            // Si existe, actualizar y reactivar
            UserDeviceToken token = existingToken.get();
            token.setIsActive(true);
            token.setDeviceType(deviceType);
            token.setDeviceName(deviceName);
            token.setUpdatedAt(LocalDateTime.now());
            
            log.info("Token de dispositivo actualizado para usuario {}: {}", userId, deviceToken);
            return deviceTokenRepository.save(token);
        } else {
            // Crear nuevo token
            UserDeviceToken newToken = new UserDeviceToken();
            newToken.setUser(user);
            newToken.setDeviceToken(deviceToken);
            newToken.setDeviceType(deviceType);
            newToken.setDeviceName(deviceName);
            newToken.setIsActive(true);
            
            log.info("Nuevo token de dispositivo registrado para usuario {}: {}", userId, deviceToken);
            return deviceTokenRepository.save(newToken);
        }
    }

    @Override
    @Transactional
    public UserDeviceToken updateDeviceToken(Long userId, String oldToken, String newToken) {
        UserDeviceToken deviceToken = deviceTokenRepository
            .findByUserIdAndDeviceToken(userId, oldToken)
            .orElseThrow(() -> new EntityNotFoundException("Token no encontrado para el usuario"));

        deviceToken.setDeviceToken(newToken);
        deviceToken.setUpdatedAt(LocalDateTime.now());
        
        log.info("Token actualizado para usuario {}: {} -> {}", userId, oldToken, newToken);
        return deviceTokenRepository.save(deviceToken);
    }

    @Override
    @Transactional
    public void deactivateDeviceToken(String deviceToken) {
        deviceTokenRepository.deactivateToken(deviceToken);
        log.info("Token desactivado: {}", deviceToken);
    }

    @Override
    @Transactional
    public void deactivateAllUserTokens(Long userId) {
        deviceTokenRepository.deactivateAllTokensForUser(userId);
        log.info("Todos los tokens desactivados para usuario: {}", userId);
    }

    @Override
    public List<UserDeviceToken> getUserActiveTokens(Long userId) {
        return deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Override
    public String getLatestUserToken(Long userId) {
        // Prioridad: Android > iOS > Web
        Optional<UserDeviceToken> androidToken = deviceTokenRepository
            .findLatestTokenByUserAndDeviceType(userId, UserDeviceToken.DeviceType.ANDROID);
        if (androidToken.isPresent()) {
            return androidToken.get().getDeviceToken();
        }
        
        Optional<UserDeviceToken> iosToken = deviceTokenRepository
            .findLatestTokenByUserAndDeviceType(userId, UserDeviceToken.DeviceType.IOS);
        if (iosToken.isPresent()) {
            return iosToken.get().getDeviceToken();
        }
        
        Optional<UserDeviceToken> webToken = deviceTokenRepository
            .findLatestTokenByUserAndDeviceType(userId, UserDeviceToken.DeviceType.WEB);
        if (webToken.isPresent()) {
            return webToken.get().getDeviceToken();
        }
        
        return null;
    }

    @Override
    public boolean isTokenValid(String deviceToken) {
        if (deviceToken == null || deviceToken.trim().isEmpty()) {
            return false;
        }
        
        // Verificar si existe en la BD y está activo
        boolean existsInDb = deviceTokenRepository.existsByDeviceToken(deviceToken);
        if (!existsInDb) {
            return false;
        }
        
        // Verificar con Firebase
        try {
            return pushNotificationService.isTokenValid(deviceToken);
        } catch (Exception e) {
            log.warn("Error verificando token con Firebase: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void cleanupOldTokens() {
        // Eliminar tokens inactivos más antiguos de 30 días
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        
        try {
            deviceTokenRepository.deleteInactiveTokensOlderThan(cutoffDate);
            log.info("Limpieza de tokens antiguos completada");
        } catch (Exception e) {
            log.error("Error durante la limpieza de tokens: {}", e.getMessage());
        }
    }
} 