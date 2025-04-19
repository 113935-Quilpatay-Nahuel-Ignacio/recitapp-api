package com.recitapp.recitapp_api.modules.artist.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "artist_genres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistGenre {

    @Id
    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @Id
    @ManyToOne
    @JoinColumn(name = "genre_id")
    private MusicGenre genre;

    @Embeddable
    public static class ArtistGenreId implements java.io.Serializable {
        @Column(name = "artist_id")
        private Long artistId;

        @Column(name = "genre_id")
        private Long genreId;

        // equals and hashCode methods

    }

    @EmbeddedId
    private ArtistGenreId id = new ArtistGenreId();
}