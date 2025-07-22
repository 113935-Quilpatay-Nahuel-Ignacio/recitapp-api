package com.recitapp.recitapp_api.modules.notification.controller;


import com.recitapp.recitapp_api.modules.notification.dto.NotificationChannelDTO;
import com.recitapp.recitapp_api.modules.notification.dto.NotificationTypeDTO;
import com.recitapp.recitapp_api.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class NotificationAdminController {

    private final NotificationService notificationService;

    @GetMapping("/channels")
    public ResponseEntity<List<NotificationChannelDTO>> getAllNotificationChannels() {
        return ResponseEntity.ok(notificationService.getAllNotificationChannels());
    }

    @GetMapping("/channels/active")
    public ResponseEntity<List<NotificationChannelDTO>> getActiveNotificationChannels() {
        return ResponseEntity.ok(notificationService.getActiveNotificationChannels());
    }

    @PostMapping("/channels")
    public ResponseEntity<NotificationChannelDTO> createNotificationChannel(
            @RequestBody NotificationChannelDTO channelDTO) {
        return new ResponseEntity<>(notificationService.createNotificationChannel(channelDTO), HttpStatus.CREATED);
    }

    @PutMapping("/channels/{channelId}")
    public ResponseEntity<NotificationChannelDTO> updateNotificationChannel(
            @PathVariable Long channelId,
            @RequestBody NotificationChannelDTO channelDTO) {
        return ResponseEntity.ok(notificationService.updateNotificationChannel(channelId, channelDTO));
    }

    @DeleteMapping("/channels/{channelId}")
    public ResponseEntity<Void> deleteNotificationChannel(@PathVariable Long channelId) {
        notificationService.deleteNotificationChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/types")
    public ResponseEntity<List<NotificationTypeDTO>> getAllNotificationTypes() {
        return ResponseEntity.ok(notificationService.getAllNotificationTypes());
    }

    @PostMapping("/types")
    public ResponseEntity<NotificationTypeDTO> createNotificationType(
            @RequestBody NotificationTypeDTO typeDTO) {
        return new ResponseEntity<>(notificationService.createNotificationType(typeDTO), HttpStatus.CREATED);
    }

    @PutMapping("/types/{typeId}")
    public ResponseEntity<NotificationTypeDTO> updateNotificationType(
            @PathVariable Long typeId,
            @RequestBody NotificationTypeDTO typeDTO) {
        return ResponseEntity.ok(notificationService.updateNotificationType(typeId, typeDTO));
    }

    @PostMapping("/generate-recommendations")
    public ResponseEntity<Void> generateWeeklyRecommendations() {
        notificationService.sendWeeklyRecommendations();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-low-availability")
    public ResponseEntity<Void> checkLowAvailability() {
        notificationService.checkAndSendLowAvailabilityAlerts();
        return ResponseEntity.noContent().build();
    }
}