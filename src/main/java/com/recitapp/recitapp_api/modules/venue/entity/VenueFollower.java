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


