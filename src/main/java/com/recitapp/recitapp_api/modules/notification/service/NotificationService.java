package com.recitapp.recitapp_api.modules.notification.service;

import com.recitapp.recitapp_api.modules.notification.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {
    // User notification preferences
    NotificationPreferenceDTO getUserNotificationPreferences(Long userId);
    NotificationPreferenceDTO updateUserNotificationPreferences(Long userId, NotificationPreferenceDTO preferencesDTO);

    // Event alerts
    void sendNewEventAlert(Long eventId, List<Long> followerUserIds);
    void sendNewEventAlertToArtistFollowers(Long artistId, Long eventId);
    void sendNewEventAlertToVenueFollowers(Long venueId, Long eventId);

    // Availability notifications
    void sendLowAvailabilityAlert(Long eventId, Integer remainingTickets);
    void checkAndSendLowAvailabilityAlerts();

    // Notification history
    List<NotificationDTO> getUserNotificationHistory(Long userId);
    List<NotificationDTO> getUserNotificationHistory(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    List<NotificationDTO> getUnreadNotifications(Long userId);
    NotificationSummaryDTO getNotificationSummary(Long userId);
    NotificationDTO markAsRead(Long notificationId);
    void markMultipleAsRead(Long userId, List<Long> notificationIds);

    // Event changes notifications
    void sendEventCancellationNotification(Long eventId);
    void sendEventModificationNotification(Long eventId, String changeDescription);
    void sendTicketCancellationNotification(Long ticketId);

    // Recommendations
    void sendPersonalizedRecommendations(Long userId);
    void sendWeeklyRecommendations();
    void sendGenreBasedRecommendations(Long userId, List<String> preferredGenres);

    // Notification channels management
    List<NotificationChannelDTO> getAllNotificationChannels();
    NotificationChannelDTO createNotificationChannel(NotificationChannelDTO channelDTO);
    NotificationChannelDTO updateNotificationChannel(Long channelId, NotificationChannelDTO channelDTO);
    void deleteNotificationChannel(Long channelId);
    List<NotificationChannelDTO> getActiveNotificationChannels();

    // Notification creation and management
    NotificationDTO createNotification(NotificationCreateDTO createDTO);
    void sendBulkNotification(BulkNotificationDTO bulkDTO);

    // Delete notifications
    void deleteNotification(Long notificationId);
    void deleteMultipleNotifications(Long userId, List<Long> notificationIds);
    void deleteReadNotifications(Long userId);

    // Query notifications by relation
    List<NotificationDTO> getNotificationsByEvent(Long eventId);
    List<NotificationDTO> getNotificationsByArtist(Long artistId);
    List<NotificationDTO> getNotificationsByVenue(Long venueId);

    // Notification types management
    List<NotificationTypeDTO> getAllNotificationTypes();
    NotificationTypeDTO createNotificationType(NotificationTypeDTO typeDTO);
    NotificationTypeDTO updateNotificationType(Long typeId, NotificationTypeDTO typeDTO);
}