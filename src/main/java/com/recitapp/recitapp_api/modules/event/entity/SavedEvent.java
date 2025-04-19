package com.recitapp.recitapp_api.modules.event.entity;

import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedEvent {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "saved_date")
    private LocalDateTime savedDate;

    // Configure composite primary key
    @Embeddable
    public static class SavedEventId implements java.io.Serializable {
        @Column(name = "user_id")
        private Long userId;

        @Column(name = "event_id")
        private Long eventId;

        // equals and hashCode methods
    }

    @EmbeddedId
    private SavedEventId id = new SavedEventId();

    @PrePersist
    protected void onCreate() {
        savedDate = LocalDateTime.now();
    }
}
