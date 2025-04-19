package com.recitapp.recitapp_api.modules.event.entity;

import com.recitapp.recitapp_api.modules.artist.entity.Artist;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_artists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventArtist {

    @Id
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Id
    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "appearance_order")
    private Integer appearanceOrder;

    // Configure composite primary key
    @Embeddable
    public static class EventArtistId implements java.io.Serializable {
        @Column(name = "event_id")
        private Long eventId;

        @Column(name = "artist_id")
        private Long artistId;

        // equals and hashCode methods
    }

    @EmbeddedId
    private EventArtistId id = new EventArtistId();
}
