package com.recitapp.recitapp_api.modules.venue.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "venue_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueStatistics {

    @Id
    @OneToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Column(name = "total_followers")
    private Integer totalFollowers;

    @Column(name = "total_events")
    private Integer totalEvents;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }
}
