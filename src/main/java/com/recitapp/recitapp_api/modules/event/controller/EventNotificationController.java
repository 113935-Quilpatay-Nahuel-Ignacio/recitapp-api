package com.recitapp.recitapp_api.modules.event.controller;

import com.recitapp.recitapp_api.modules.event.dto.ArtistNotificationRequest;
import com.recitapp.recitapp_api.modules.event.dto.VenueNotificationRequest;
import com.recitapp.recitapp_api.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/{eventId}/notify-artist-followers")
    public ResponseEntity<Void> notifyArtistFollowers(
            @PathVariable Long eventId,
            @RequestBody ArtistNotificationRequest request) {

        notificationService.sendNewEventAlertToArtistFollowers(request.getArtistId(), eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/notify-venue-followers")
    public ResponseEntity<Void> notifyVenueFollowers(
            @PathVariable Long eventId,
            @RequestBody VenueNotificationRequest request) {

        notificationService.sendNewEventAlertToVenueFollowers(request.getVenueId(), eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/notify-modification")
    public ResponseEntity<Void> notifyEventModification(
            @PathVariable Long eventId,
            @RequestParam String changeDescription) {

        notificationService.sendEventModificationNotification(eventId, changeDescription);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/notify-cancellation")
    public ResponseEntity<Void> notifyEventCancellation(
            @PathVariable Long eventId) {

        notificationService.sendEventCancellationNotification(eventId);
        return ResponseEntity.noContent().build();
    }
}