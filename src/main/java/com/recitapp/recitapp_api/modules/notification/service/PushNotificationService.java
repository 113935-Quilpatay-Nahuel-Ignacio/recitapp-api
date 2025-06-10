package com.recitapp.recitapp_api.modules.notification.service;

import java.util.List;
import java.util.Map;

public interface PushNotificationService {
    
    /**
     * Send push notification to a single device
     */
    String sendToDevice(String deviceToken, String title, String body, Map<String, String> data);
    
    /**
     * Send push notification to multiple devices
     */
    void sendToMultipleDevices(List<String> deviceTokens, String title, String body, Map<String, String> data);
    
    /**
     * Send push notification to a topic
     */
    String sendToTopic(String topic, String title, String body, Map<String, String> data);
    
    /**
     * Send custom push notification with image and priority
     */
    String sendCustomNotification(String deviceToken, String title, String body, 
                                Map<String, String> data, String imageUrl, String priority);
    
    /**
     * Validate if a device token is valid
     */
    boolean isTokenValid(String deviceToken);
}
