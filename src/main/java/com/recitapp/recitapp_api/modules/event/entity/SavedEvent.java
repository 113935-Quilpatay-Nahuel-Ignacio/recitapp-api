package com.recitapp.recitapp_api.modules.event.entity;

import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "saved_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedEvent {

    @EmbeddedId
    private SavedEventId id = new SavedEventId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "saved_date")
    private LocalDateTime savedDate;

    @PrePersist
    protected void onCreate() {
        if (savedDate == null) {
            savedDate = LocalDateTime.now();
        }
    }
}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class SavedEventId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "event_id")
    private Long eventId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedEventId that = (SavedEventId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, eventId);
    }
}
