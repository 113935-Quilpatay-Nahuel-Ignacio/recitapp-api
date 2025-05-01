package com.recitapp.recitapp_api.modules.event.entity;

import com.recitapp.recitapp_api.modules.artist.entity.Artist;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "event_artists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventArtist {

    @EmbeddedId
    private EventArtistId id = new EventArtistId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artistId")
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "appearance_order")
    private Integer appearanceOrder;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventArtistId implements Serializable {
        private static final long serialVersionUID = 1L;

        @Column(name = "event_id")
        private Long eventId;

        @Column(name = "artist_id")
        private Long artistId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EventArtistId that = (EventArtistId) o;
            return Objects.equals(eventId, that.eventId) &&
                    Objects.equals(artistId, that.artistId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventId, artistId);
        }
    }
}


