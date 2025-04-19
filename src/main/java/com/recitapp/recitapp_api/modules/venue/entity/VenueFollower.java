package com.recitapp.recitapp_api.modules.venue.entity;

import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "venue_followers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueFollower {

    @EmbeddedId
    private VenueFollowerId id = new VenueFollowerId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("venueId")
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Column(name = "follow_date")
    private LocalDateTime followDate;

    @PrePersist
    protected void onCreate() {
        if (followDate == null) {
            followDate = LocalDateTime.now();
        }
    }
}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class VenueFollowerId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "venue_id")
    private Long venueId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VenueFollowerId that = (VenueFollowerId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(venueId, that.venueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, venueId);
    }
}
