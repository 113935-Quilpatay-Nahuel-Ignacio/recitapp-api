package com.recitapp.recitapp_api.modules.artist.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "artist_genres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistGenre {

    @EmbeddedId
    private ArtistGenreId id = new ArtistGenreId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artistId")
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("genreId")
    @JoinColumn(name = "genre_id")
    private MusicGenre genre;
}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class ArtistGenreId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "artist_id")
    private Long artistId;

    @Column(name = "genre_id")
    private Long genreId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtistGenreId that = (ArtistGenreId) o;
        return Objects.equals(artistId, that.artistId) &&
                Objects.equals(genreId, that.genreId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artistId, genreId);
    }
}