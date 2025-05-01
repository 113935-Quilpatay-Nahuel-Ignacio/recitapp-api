package com.recitapp.recitapp_api.modules.notification.service;

import com.recitapp.recitapp_api.modules.notification.dto.NotificationPreferenceDTO;

public interface NotificationService {
    NotificationPreferenceDTO getUserNotificationPreferences(Long userId);
    NotificationPreferenceDTO updateUserNotificationPreferences(Long userId, NotificationPreferenceDTO preferencesDTO);
}
