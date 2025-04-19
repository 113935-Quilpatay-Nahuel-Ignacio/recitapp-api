package com.recitapp.recitapp_api.modules.notification.entity;

import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "receive_reminder_emails")
    private Boolean receiveReminderEmails;

    @Column(name = "receive_event_push")
    private Boolean receiveEventPush;

    @Column(name = "receive_artist_push")
    private Boolean receiveArtistPush;

    @Column(name = "receive_availability_push")
    private Boolean receiveAvailabilityPush;

    @Column(name = "receive_weekly_newsletter")
    private Boolean receiveWeeklyNewsletter;

    @PrePersist
    protected void onCreate() {
        if (receiveReminderEmails == null) receiveReminderEmails = true;
        if (receiveEventPush == null) receiveEventPush = true;
        if (receiveArtistPush == null) receiveArtistPush = true;
        if (receiveAvailabilityPush == null) receiveAvailabilityPush = true;
        if (receiveWeeklyNewsletter == null) receiveWeeklyNewsletter = true;
    }
}

