package com.recitapp.recitapp_api.modules.notification.service.impl;

import com.google.firebase.messaging.*;
import com.recitapp.recitapp_api.modules.notification.service.PushNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    @Override
    public String sendToDevice(String deviceToken, String title, String body, Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                .setToken(deviceToken)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            // Configuración para Android
            messageBuilder.setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                    .build())
                .build());

            // Configuración para iOS
            messageBuilder.setApnsConfig(ApnsConfig.builder()
                .setAps(Aps.builder()
                    .setAlert(ApsAlert.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                    .setBadge(1)
                    .setSound("default")
                    .build())
                .build());

            Message message = messageBuilder.build();
            String response = FirebaseMessaging.getInstance().send(message);
            
            log.info("Push notification sent successfully to device: {}, response: {}", deviceToken, response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification to device {}: {}", deviceToken, e.getMessage());
            throw new RuntimeException("Failed to send push notification", e);
        }
    }

    @Override
    public void sendToMultipleDevices(List<String> deviceTokens, String title, String body, Map<String, String> data) {
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            log.warn("No device tokens provided for bulk notification");
            return;
        }

        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
            .addAllTokens(deviceTokens)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build());

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        // Configuración para Android
        messageBuilder.setAndroidConfig(AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .setNotification(AndroidNotification.builder()
                .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                .build())
            .build());

        // Configuración para iOS
        messageBuilder.setApnsConfig(ApnsConfig.builder()
            .setAps(Aps.builder()
                .setAlert(ApsAlert.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .setBadge(1)
                .setSound("default")
                .build())
            .build());

        try {
            MulticastMessage message = messageBuilder.build();
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            
            log.info("Bulk push notification sent to {} devices. Success: {}, Failure: {}", 
                deviceTokens.size(), response.getSuccessCount(), response.getFailureCount());
                
            // Log failed tokens for cleanup
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        log.warn("Failed to send to token {}: {}", 
                            deviceTokens.get(i), responses.get(i).getException().getMessage());
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send bulk push notifications: {}", e.getMessage());
            throw new RuntimeException("Failed to send bulk push notifications", e);
        }
    }

    @Override
    public String sendToTopic(String topic, String title, String body, Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();
            String response = FirebaseMessaging.getInstance().send(message);
            
            log.info("Push notification sent successfully to topic: {}, response: {}", topic, response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification to topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Failed to send push notification to topic", e);
        }
    }

    @Override
    public String sendCustomNotification(String deviceToken, String title, String body, 
                                       Map<String, String> data, String imageUrl, String priority) {
        try {
            Message.Builder messageBuilder = Message.builder()
                .setToken(deviceToken)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .setImage(imageUrl)
                    .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            // Configuración personalizada para Android
            AndroidConfig.Priority androidPriority = "high".equals(priority) ? 
                AndroidConfig.Priority.HIGH : AndroidConfig.Priority.NORMAL;
                
            messageBuilder.setAndroidConfig(AndroidConfig.builder()
                .setPriority(androidPriority)
                .setNotification(AndroidNotification.builder()
                    .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                    .setImageUrl(imageUrl)
                    .build())
                .build());

            // Configuración personalizada para iOS
            messageBuilder.setApnsConfig(ApnsConfig.builder()
                .setAps(Aps.builder()
                    .setAlert(ApsAlert.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                    .setBadge(1)
                    .setSound("default")
                    .build())
                .build());

            Message message = messageBuilder.build();
            String response = FirebaseMessaging.getInstance().send(message);
            
            log.info("Custom push notification sent successfully to device: {}, response: {}", deviceToken, response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send custom push notification to device {}: {}", deviceToken, e.getMessage());
            throw new RuntimeException("Failed to send custom push notification", e);
        }
    }

    @Override
    public boolean isTokenValid(String deviceToken) {
        try {
            // Intentamos enviar un mensaje de prueba sin contenido para validar el token
            Message message = Message.builder()
                .setToken(deviceToken)
                .putData("test", "validation")
                .build();
                
            FirebaseMessaging.getInstance().send(message);
            return true;
        } catch (FirebaseMessagingException e) {
            log.warn("Invalid device token: {}, error: {}", deviceToken, e.getMessage());
            return false;
        }
    }
} 