package com.recitapp.recitapp_api.modules.notification.service.impl;

import com.google.firebase.messaging.*;
import com.recitapp.recitapp_api.modules.notification.service.PushNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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

        // Usar sendEach en lugar de sendMulticast (método más moderno)
        for (String deviceToken : deviceTokens) {
            try {
                sendToDevice(deviceToken, title, body, data);
                log.debug("Successfully sent notification to device: {}", deviceToken);
            } catch (Exception e) {
                log.error("Failed to send notification to device {}: {}", deviceToken, e.getMessage());
            }
        }
        
        log.info("Bulk push notification completed for {} devices", deviceTokens.size());
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
                
            AndroidNotification.Builder androidBuilder = AndroidNotification.builder()
                .setClickAction("FLUTTER_NOTIFICATION_CLICK");
            
            // Solo agregar imagen si está disponible
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Nota: setImageUrl no está disponible en todas las versiones
                // Usar la imagen en el notification principal
            }
                
            messageBuilder.setAndroidConfig(AndroidConfig.builder()
                .setPriority(androidPriority)
                .setNotification(androidBuilder.build())
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
            // Crear un mensaje de prueba mínimo
            Message message = Message.builder()
                .setToken(deviceToken)
                .putData("test", "validation")
                .build();
                
            // Intentar enviar el mensaje de validación
            FirebaseMessaging.getInstance().send(message);
            return true;
        } catch (FirebaseMessagingException e) {
            // Si hay error específico de token inválido
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
                e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                log.warn("Invalid device token: {}, error: {}", deviceToken, e.getMessage());
                return false;
            }
            // Para otros errores, asumir que el token es válido pero hay otros problemas
            log.warn("Could not validate token {}, assuming valid: {}", deviceToken, e.getMessage());
            return true;
        } catch (Exception e) {
            log.error("Unexpected error validating token {}: {}", deviceToken, e.getMessage());
            return false;
        }
    }
} 