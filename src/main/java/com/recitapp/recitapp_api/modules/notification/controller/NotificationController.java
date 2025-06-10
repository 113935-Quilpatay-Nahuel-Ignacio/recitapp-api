package com.recitapp.recitapp_api.modules.notification.controller;

import com.recitapp.recitapp_api.modules.notification.dto.*;
import com.recitapp.recitapp_api.modules.notification.dto.NewEventEmailDTO;
import com.recitapp.recitapp_api.modules.notification.dto.LowAvailabilityEmailDTO;
import com.recitapp.recitapp_api.modules.notification.service.NotificationService;
import com.recitapp.recitapp_api.modules.notification.service.EmailService;
import com.recitapp.recitapp_api.modules.notification.service.PushNotificationService;
import com.recitapp.recitapp_api.modules.notification.service.SmsService;
// import com.recitapp.recitapp_api.modules.notification.service.WhatsAppService; // DESACTIVADO: Requiere Twilio de pago
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final PushNotificationService pushNotificationService;
    private final SmsService smsService;
    // private final WhatsAppService whatsAppService; // DESACTIVADO: Requiere Twilio de pago

    @GetMapping("/user/{userId}/preferences")
    public ResponseEntity<NotificationPreferenceDTO> getUserNotificationPreferences(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotificationPreferences(userId));
    }

    @PutMapping("/user/{userId}/preferences")
    public ResponseEntity<NotificationPreferenceDTO> updateUserNotificationPreferences(
            @PathVariable Long userId,
            @RequestBody NotificationPreferenceDTO preferencesDTO) {
        return ResponseEntity.ok(notificationService.updateUserNotificationPreferences(userId, preferencesDTO));
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<NotificationDTO>> getUserNotificationHistory(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(notificationService.getUserNotificationHistory(userId, startDate, endDate));
        } else {
            return ResponseEntity.ok(notificationService.getUserNotificationHistory(userId));
        }
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<NotificationSummaryDTO> getNotificationSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationSummary(userId));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markNotificationAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @PatchMapping("/user/{userId}/read-multiple")
    public ResponseEntity<Void> markMultipleAsRead(
            @PathVariable Long userId,
            @RequestBody List<Long> notificationIds) {
        notificationService.markMultipleAsRead(userId, notificationIds);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(@RequestBody NotificationCreateDTO createDTO) {
        return new ResponseEntity<>(notificationService.createNotification(createDTO), HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Void> sendBulkNotification(@RequestBody BulkNotificationDTO bulkDTO) {
        notificationService.sendBulkNotification(bulkDTO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(notificationService.getNotificationsByEvent(eventId));
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByArtist(@PathVariable Long artistId) {
        return ResponseEntity.ok(notificationService.getNotificationsByArtist(artistId));
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(notificationService.getNotificationsByVenue(venueId));
    }

    // ===== ENDPOINTS DE TESTING Y GESTIÓN =====

    @PostMapping("/email/new-event")
    public ResponseEntity<String> sendNewEventEmail(@RequestBody NewEventEmailDTO dto) {
        try {
            emailService.sendNewEventEmail(
                dto.getRecipientEmail(),
                dto.getEventName(),
                dto.getArtistName(),
                dto.getEventDate(),
                dto.getVenueName()
            );
            return ResponseEntity.ok("Email de nuevo evento enviado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando email de nuevo evento: " + e.getMessage());
        }
    }

    @PostMapping("/email/low-availability")
    public ResponseEntity<String> sendLowAvailabilityEmail(@RequestBody LowAvailabilityEmailDTO dto) {
        try {
            emailService.sendLowAvailabilityEmail(
                dto.getRecipientEmail(),
                dto.getEventName(),
                dto.getTicketsRemaining()
            );
            return ResponseEntity.ok("Email de baja disponibilidad enviado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando email de baja disponibilidad: " + e.getMessage());
        }
    }

    @PostMapping("/test/email")
    public ResponseEntity<String> testEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String message) {
        try {
            emailService.sendSimpleEmail(to, subject, message);
            return ResponseEntity.ok("Email enviado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando email: " + e.getMessage());
        }
    }

    // ===== PUSH NOTIFICATION TEST ENDPOINTS =====

    @PostMapping("/test/push")
    public ResponseEntity<String> testPushNotification(
            @RequestParam String deviceToken,
            @RequestParam String title,
            @RequestParam String body) {
        try {
            String response = pushNotificationService.sendToDevice(deviceToken, title, body, null);
            return ResponseEntity.ok("Push notification enviada: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando push notification: " + e.getMessage());
        }
    }

    @PostMapping("/test/push/advanced")
    public ResponseEntity<String> testAdvancedPushNotification(
            @RequestParam String deviceToken,
            @RequestParam String title,
            @RequestParam String body,
            @RequestParam(required = false) String eventId,
            @RequestParam(required = false) String actionUrl,
            @RequestParam(required = false) String imageUrl) {
        try {
            java.util.Map<String, String> data = new java.util.HashMap<>();
            if (eventId != null) data.put("eventId", eventId);
            if (actionUrl != null) data.put("actionUrl", actionUrl);
            data.put("type", "TEST_NOTIFICATION");
            data.put("timestamp", String.valueOf(System.currentTimeMillis()));

            String response;
            if (imageUrl != null && !imageUrl.isEmpty()) {
                response = pushNotificationService.sendCustomNotification(deviceToken, title, body, data, imageUrl, "high");
            } else {
                response = pushNotificationService.sendToDevice(deviceToken, title, body, data);
            }
            
            return ResponseEntity.ok("Push notification avanzada enviada: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando push notification avanzada: " + e.getMessage());
        }
    }

    @PostMapping("/test/push/topic")
    public ResponseEntity<String> testTopicPushNotification(
            @RequestParam String topic,
            @RequestParam String title,
            @RequestParam String body) {
        try {
            java.util.Map<String, String> data = new java.util.HashMap<>();
            data.put("type", "TOPIC_NOTIFICATION");
            data.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            String response = pushNotificationService.sendToTopic(topic, title, body, data);
            return ResponseEntity.ok("Push notification enviada al topic '" + topic + "': " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando push notification al topic: " + e.getMessage());
        }
    }

    @PostMapping("/test/push/multiple")
    public ResponseEntity<String> testMultiplePushNotification(
            @RequestBody java.util.List<String> deviceTokens,
            @RequestParam String title,
            @RequestParam String body) {
        try {
            java.util.Map<String, String> data = new java.util.HashMap<>();
            data.put("type", "BULK_NOTIFICATION");
            data.put("recipients", String.valueOf(deviceTokens.size()));
            data.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            pushNotificationService.sendToMultipleDevices(deviceTokens, title, body, data);
            return ResponseEntity.ok("Push notifications enviadas a " + deviceTokens.size() + " dispositivos");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando push notifications múltiples: " + e.getMessage());
        }
    }

    @PostMapping("/test/push/validate-token")
    public ResponseEntity<String> validateDeviceToken(@RequestParam String deviceToken) {
        try {
            boolean isValid = pushNotificationService.isTokenValid(deviceToken);
            if (isValid) {
                return ResponseEntity.ok("✅ Token válido: " + deviceToken);
            } else {
                return ResponseEntity.badRequest()
                    .body("❌ Token inválido: " + deviceToken);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error validando token: " + e.getMessage());
        }
    }

    @PostMapping("/test/push/debug")
    public ResponseEntity<String> debugPushNotification(@RequestParam String deviceToken) {
        try {
            StringBuilder debug = new StringBuilder();
            debug.append("🔍 DEBUG PUSH NOTIFICATION\n\n");
            
            // Información del token
            debug.append("📱 Token Info:\n");
            debug.append("• Length: ").append(deviceToken.length()).append(" characters\n");
            debug.append("• Starts with: ").append(deviceToken.substring(0, Math.min(20, deviceToken.length()))).append("...\n");
            debug.append("• Contains colon: ").append(deviceToken.contains(":") ? "✅ YES" : "❌ NO").append("\n");
            debug.append("• Format looks valid: ").append(deviceToken.matches("^[A-Za-z0-9_-]+:[A-Za-z0-9_-]+$") ? "✅ YES" : "❌ NO").append("\n\n");
            
            // Validar token
            debug.append("🔐 Token Validation:\n");
            try {
                boolean isValid = pushNotificationService.isTokenValid(deviceToken);
                debug.append("• Validation result: ").append(isValid ? "✅ VALID" : "❌ INVALID").append("\n");
            } catch (Exception validationError) {
                debug.append("• Validation error: ").append(validationError.getMessage()).append("\n");
            }
            
            debug.append("\n💡 Recommendations:\n");
            if (deviceToken.length() < 100) {
                debug.append("• Token seems too short - generate a real FCM token\n");
            }
            if (!deviceToken.contains(":")) {
                debug.append("• Token should contain ':' separator\n");
            }
            if (deviceToken.contains("example") || deviceToken.contains("test") || deviceToken.contains("1234567890")) {
                debug.append("• This appears to be a test/example token - use a real FCM token\n");
            }
            
            debug.append("\n🚀 Next Steps:\n");
            debug.append("1. Use the token generator: test-token-generator.html\n");
            debug.append("2. Or use browser console with Firebase SDK\n");
            debug.append("3. Make sure notifications are enabled in browser\n");
            debug.append("4. Use HTTPS or localhost for token generation\n");
            
            return ResponseEntity.ok(debug.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error in debug: " + e.getMessage());
        }
    }

    @PostMapping("/test/push/event-notification")
    public ResponseEntity<String> testEventPushNotification(
            @RequestParam String deviceToken,
            @RequestParam(required = false, defaultValue = "1") String eventId,
            @RequestParam(required = false, defaultValue = "Concierto de Prueba") String eventName,
            @RequestParam(required = false, defaultValue = "Artista de Prueba") String artistName) {
        try {
            String title = "🎵 ¡Nuevo Evento!";
            String body = String.format("%s - %s ya está disponible", eventName, artistName);
            
            java.util.Map<String, String> data = new java.util.HashMap<>();
            data.put("type", "NEW_EVENT");
            data.put("eventId", eventId);
            data.put("eventName", eventName);
            data.put("artistName", artistName);
            data.put("actionUrl", "/events/" + eventId);
            
            String response = pushNotificationService.sendToDevice(deviceToken, title, body, data);
            return ResponseEntity.ok("Notificación de evento enviada: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando notificación de evento: " + e.getMessage());
        }
    }

    @PostMapping("/test/sms")
    public ResponseEntity<String> testSms(
            @RequestParam String phoneNumber,
            @RequestParam String message) {
        try {
            String response = smsService.sendSms(phoneNumber, message);
            return ResponseEntity.ok("SMS enviado exitosamente con SID: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando SMS: " + e.getMessage());
        }
    }

    @PostMapping("/test/sms/template")
    public ResponseEntity<String> testSmsTemplate(
            @RequestParam String phoneNumber,
            @RequestParam String templateId,
            @RequestParam(required = false, defaultValue = "Evento de Prueba") String eventName,
            @RequestParam(required = false, defaultValue = "Venue de Prueba") String venueName) {
        try {
            String response = smsService.sendTemplateSms(phoneNumber, templateId, eventName, venueName);
            return ResponseEntity.ok("SMS template enviado exitosamente con SID: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando SMS template: " + e.getMessage());
        }
    }

    // ==========================================
    // WHATSAPP ENDPOINTS - TEMPORALMENTE DESACTIVADOS
    // ==========================================
    // NOTA: WhatsApp requiere cuenta Twilio de pago
    // Para activar: descomentar los endpoints y el servicio
    
    /*
    @PostMapping("/test/whatsapp")
    public ResponseEntity<String> testWhatsApp(
            @RequestParam String phoneNumber,
            @RequestParam String message) {
        try {
            boolean success = whatsAppService.sendWhatsAppMessage(phoneNumber, message);
            if (success) {
                return ResponseEntity.ok("Mensaje WhatsApp enviado exitosamente");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error enviando WhatsApp: El mensaje no se pudo enviar");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando WhatsApp: " + e.getMessage());
        }
    }
    */

    /*
    @PostMapping("/test/whatsapp/template")
    public ResponseEntity<String> testWhatsAppTemplate(
            @RequestParam String phoneNumber,
            @RequestParam String templateName,
            @RequestParam(required = false, defaultValue = "Evento de Prueba") String eventName,
            @RequestParam(required = false, defaultValue = "Artista de Prueba") String artistName) {
        try {
            java.util.Map<String, String> parameters = new java.util.HashMap<>();
            parameters.put("eventName", eventName);
            parameters.put("artistName", artistName);
            parameters.put("eventDate", "2024-12-31");
            parameters.put("venueName", "Venue de Prueba");

            boolean success = whatsAppService.sendTemplateMessage(phoneNumber, templateName, parameters);
            if (success) {
                return ResponseEntity.ok("Template WhatsApp enviado exitosamente: " + templateName);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error enviando template WhatsApp");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando template WhatsApp: " + e.getMessage());
        }
    }
    */

    @PostMapping("/send/reminder/{eventId}")
    public ResponseEntity<String> sendEventReminder(@PathVariable Long eventId) {
        try {
            // Implementar lógica de recordatorio
            return ResponseEntity.ok("Recordatorios enviados para el evento " + eventId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando recordatorios: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notification service is running");
    }

    @GetMapping("/test/push/config")
    public ResponseEntity<String> testPushConfig() {
        try {
            StringBuilder configInfo = new StringBuilder();
            configInfo.append("🔧 Push Notification Configuration Status:\n\n");
            
            // Check if service is available
            boolean serviceAvailable = pushNotificationService != null;
            configInfo.append("Service Available: ").append(serviceAvailable ? "✅ YES" : "❌ NO").append("\n");
            
            if (serviceAvailable) {
                configInfo.append("Service Type: Firebase Cloud Messaging (FCM)\n");
                configInfo.append("Configuration: Check server logs for Firebase initialization status\n\n");
                
                configInfo.append("📋 Available Test Endpoints:\n");
                configInfo.append("• POST /api/notifications/test/push - Basic push test\n");
                configInfo.append("• POST /api/notifications/test/push/advanced - Advanced push with data\n");
                configInfo.append("• POST /api/notifications/test/push/topic - Send to topic\n");
                configInfo.append("• POST /api/notifications/test/push/multiple - Send to multiple devices\n");
                configInfo.append("• POST /api/notifications/test/push/validate-token - Validate device token\n");
                configInfo.append("• POST /api/notifications/test/push/event-notification - Event notification test\n\n");
                
                configInfo.append("🔑 Token Generator:\n");
                configInfo.append("• Open: test-token-generator.html in your browser\n");
                configInfo.append("• URL: file:///path/to/recitapp-front/src/app/test-token-generator.html\n\n");
                
                configInfo.append("📱 Firebase Project Info:\n");
                configInfo.append("• Project ID: recitapp-niquilpatay\n");
                configInfo.append("• Sender ID: 465296679606\n");
                configInfo.append("• VAPID Key: BK7hiDOD5gocnmqmqiJ-nRXT2oo3JAGrfntmNohq5abTF1osFrmtCVY-nTGSXVwjiffWl3a7PzcN6MD2XptwOx0\n\n");
                
                configInfo.append("📝 Required for Push Notifications:\n");
                configInfo.append("1. Firebase project configured\n");
                configInfo.append("2. Firebase Admin SDK service account key\n");
                configInfo.append("3. Valid device tokens from client apps\n");
                configInfo.append("4. FCM enabled in Firebase console\n");
            }
            
            return ResponseEntity.ok(configInfo.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error checking push configuration: " + e.getMessage());
        }
    }

    /*
    @GetMapping("/test/whatsapp/config")
    public ResponseEntity<String> testWhatsAppConfig() {
        try {
            // Test configuration without sending message
            StringBuilder configInfo = new StringBuilder();
            configInfo.append("🔧 WhatsApp Configuration Status:\n\n");
            
            // Check if service is available
            boolean serviceAvailable = whatsAppService != null;
            configInfo.append("Service Available: ").append(serviceAvailable ? "✅ YES" : "❌ NO").append("\n");
            
            if (serviceAvailable) {
                // Test basic connectivity (this will show initialization status in logs)
                configInfo.append("Service Type: WhatsApp via Twilio\n");
                configInfo.append("Configuration: Check server logs for detailed configuration status\n\n");
                
                configInfo.append("📋 Next Steps if not working:\n");
                configInfo.append("1. Check Twilio Console > Messaging > WhatsApp\n");
                configInfo.append("2. Verify WhatsApp Sandbox is set up\n");
                configInfo.append("3. Ensure recipient numbers are added to sandbox\n");
                configInfo.append("4. Check server logs for detailed error messages\n");
            }
            
            return ResponseEntity.ok(configInfo.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error checking WhatsApp configuration: " + e.getMessage());
        }
    }
    */
}