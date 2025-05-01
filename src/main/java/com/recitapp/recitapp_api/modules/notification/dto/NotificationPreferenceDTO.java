package com.recitapp.recitapp_api.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {
    private Boolean receiveReminderEmails;
    private Boolean receiveEventPush;
    private Boolean receiveArtistPush;
    private Boolean receiveAvailabilityPush;
    private Boolean receiveWeeklyNewsletter;
}