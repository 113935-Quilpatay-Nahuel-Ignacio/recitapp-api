package com.recitapp.recitapp_api.modules.notification.service.impl;

import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.notification.dto.NotificationPreferenceDTO;
import com.recitapp.recitapp_api.modules.notification.entity.NotificationPreference;
import com.recitapp.recitapp_api.modules.notification.repository.NotificationPreferenceRepository;
import com.recitapp.recitapp_api.modules.notification.service.NotificationService;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @Override
    public NotificationPreferenceDTO getUserNotificationPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RecitappException("Usuario no encontrado con ID: " + userId));

        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreference(user));

        return mapToDTO(preference);
    }

    @Override
    @Transactional
    public NotificationPreferenceDTO updateUserNotificationPreferences(Long userId, NotificationPreferenceDTO preferencesDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RecitappException("Usuario no encontrado con ID: " + userId));

        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreference(user));

        // Actualizar solo los campos que no son nulos
        if (preferencesDTO.getReceiveReminderEmails() != null) {
            preference.setReceiveReminderEmails(preferencesDTO.getReceiveReminderEmails());
        }

        if (preferencesDTO.getReceiveEventPush() != null) {
            preference.setReceiveEventPush(preferencesDTO.getReceiveEventPush());
        }

        if (preferencesDTO.getReceiveArtistPush() != null) {
            preference.setReceiveArtistPush(preferencesDTO.getReceiveArtistPush());
        }

        if (preferencesDTO.getReceiveAvailabilityPush() != null) {
            preference.setReceiveAvailabilityPush(preferencesDTO.getReceiveAvailabilityPush());
        }

        if (preferencesDTO.getReceiveWeeklyNewsletter() != null) {
            preference.setReceiveWeeklyNewsletter(preferencesDTO.getReceiveWeeklyNewsletter());
        }

        preferenceRepository.save(preference);

        return mapToDTO(preference);
    }

    private NotificationPreference createDefaultPreference(User user) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUser(user);
        return preferenceRepository.save(preference);
    }

    private NotificationPreferenceDTO mapToDTO(NotificationPreference preference) {
        return new NotificationPreferenceDTO(
                preference.getReceiveReminderEmails(),
                preference.getReceiveEventPush(),
                preference.getReceiveArtistPush(),
                preference.getReceiveAvailabilityPush(),
                preference.getReceiveWeeklyNewsletter()
        );
    }
}