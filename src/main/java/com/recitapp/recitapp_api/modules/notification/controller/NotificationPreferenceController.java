package com.recitapp.recitapp_api.modules.notification.controller;

import com.recitapp.recitapp_api.modules.notification.dto.NotificationPreferenceDTO;
import com.recitapp.recitapp_api.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationService preferenceService;

    @GetMapping
    public ResponseEntity<NotificationPreferenceDTO> getUserNotificationPreferences(@PathVariable Long userId) {
        NotificationPreferenceDTO preferences = preferenceService.getUserNotificationPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    @PutMapping
    public ResponseEntity<NotificationPreferenceDTO> updateUserNotificationPreferences(
            @PathVariable Long userId,
            @RequestBody NotificationPreferenceDTO preferencesDTO) {
        NotificationPreferenceDTO updatedPreferences = preferenceService.updateUserNotificationPreferences(userId, preferencesDTO);
        return ResponseEntity.ok(updatedPreferences);
    }
}