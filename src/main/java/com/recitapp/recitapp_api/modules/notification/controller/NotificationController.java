package com.recitapp.recitapp_api.modules.notification.controller;

import com.recitapp.recitapp_api.modules.notification.dto.*;
import com.recitapp.recitapp_api.modules.notification.service.NotificationService;
import com.recitapp.recitapp_api.modules.notification.service.EmailService;
import com.recitapp.recitapp_api.modules.notification.service.PushNotificationService;
import com.recitapp.recitapp_api.modules.notification.service.SmsService;
import com.recitapp.recitapp_api.modules.notification.service.WhatsAppService;
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
    private final WhatsAppService whatsAppService;

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
}