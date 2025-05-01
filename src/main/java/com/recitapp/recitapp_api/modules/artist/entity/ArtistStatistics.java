package com.recitapp.recitapp_api.modules.artist.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "artist_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistStatistics {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "artist_id")
    private Artist artist;

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