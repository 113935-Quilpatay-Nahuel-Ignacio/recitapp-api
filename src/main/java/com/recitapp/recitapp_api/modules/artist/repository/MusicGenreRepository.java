package com.recitapp.recitapp_api.modules.artist.repository;

import com.recitapp.recitapp_api.modules.artist.entity.MusicGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MusicGenreRepository extends JpaRepository<MusicGenre, Long> {

    Optional<MusicGenre> findByName(String name);

    boolean existsByName(String name);

    List<MusicGenre> findByNameContainingIgnoreCase(String name);

    @Query("SELECT mg FROM MusicGenre mg JOIN ArtistGenre ag ON mg.id = ag.genre.id " +
            "WHERE ag.artist.id = :artistId")
    List<MusicGenre> findGenresByArtistId(@Param("artistId") Long artistId);

    @Query("SELECT mg FROM MusicGenre mg ORDER BY " +
            "(SELECT COUNT(ag) FROM ArtistGenre ag WHERE ag.genre.id = mg.id) DESC")
    List<MusicGenre> findMostPopularGenres(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(ag) > 0 THEN true ELSE false END FROM ArtistGenre ag " +
            "WHERE ag.artist.id = :artistId AND ag.genre.id = :genreId")
    boolean existsByArtistIdAndGenreId(@Param("artistId") Long artistId, @Param("genreId") Long genreId);

    @Query("SELECT COUNT(ag.artist.id) FROM ArtistGenre ag WHERE ag.genre.id = :genreId")
    Long countArtistsByGenreId(@Param("genreId") Long genreId);
}