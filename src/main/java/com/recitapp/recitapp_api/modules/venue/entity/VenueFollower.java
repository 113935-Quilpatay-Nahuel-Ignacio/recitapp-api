package com.recitapp.recitapp_api.modules.venue.entity;

import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "venue_followers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueFollower {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Column(name = "follow_date")
    private LocalDateTime followDate;

    @Embeddable
    public static class VenueFollowerId implements java.io.Serializable {
        @Column(name = "user_id")
        private Long userId;

        @Column(name = "venue_id")
        private Long venueId;

        // equals and hashCode methods
    }

    @EmbeddedId
    private VenueFollowerId id = new VenueFollowerId();

    @PrePersist
    protected void onCreate() {
        followDate = LocalDateTime.now();
    }
}
