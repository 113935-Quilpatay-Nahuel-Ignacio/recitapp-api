package com.recitapp.recitapp_api.modules.notification.entity;

import com.recitapp.recitapp_api.modules.artist.entity.Artist;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.venue.entity.Venue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private NotificationType type;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private NotificationChannel channel;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @ManyToOne
    @JoinColumn(name = "related_event_id")
    private Event relatedEvent;

    @ManyToOne
    @JoinColumn(name = "related_artist_id")
    private Artist relatedArtist;

    @ManyToOne
    @JoinColumn(name = "related_venue_id")
    private Venue relatedVenue;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
