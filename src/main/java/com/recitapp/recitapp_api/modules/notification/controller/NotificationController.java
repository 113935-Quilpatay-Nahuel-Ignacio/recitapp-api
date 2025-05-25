package com.recitapp.recitapp_api.modules.notification.controller;

import com.recitapp.recitapp_api.modules.notification.dto.*;
import com.recitapp.recitapp_api.modules.notification.service.NotificationService;
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
}