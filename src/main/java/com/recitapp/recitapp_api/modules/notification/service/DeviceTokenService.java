package com.recitapp.recitapp_api.modules.notification.service;

import com.recitapp.recitapp_api.modules.notification.entity.UserDeviceToken;

import java.util.List;

public interface DeviceTokenService {
    
    /**
     * Registra un nuevo token de dispositivo para un usuario
     */
    UserDeviceToken registerDeviceToken(Long userId, String deviceToken, 
                                      UserDeviceToken.DeviceType deviceType, String deviceName);
    
    /**
     * Actualiza un token de dispositivo existente
     */
    UserDeviceToken updateDeviceToken(Long userId, String oldToken, String newToken);
    
    /**
     * Desactiva un token de dispositivo
     */
    void deactivateDeviceToken(String deviceToken);
    
    /**
     * Desactiva todos los tokens de un usuario
     */
    void deactivateAllUserTokens(Long userId);
    
    /**
     * Obtiene todos los tokens activos de un usuario
     */
    List<UserDeviceToken> getUserActiveTokens(Long userId);
    
    /**
     * Obtiene el token más reciente de un usuario
     */
    String getLatestUserToken(Long userId);
    
    /**
     * Verifica si un token es válido
     */
    boolean isTokenValid(String deviceToken);
    
    /**
     * Limpia tokens inactivos antiguos
     */
    void cleanupOldTokens();
} 