package com.recitapp.recitapp_api.modules.artist.repository;

import com.recitapp.recitapp_api.modules.artist.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByName(String name);
    List<Artist> findByNameContainingIgnoreCase(String name);

    List<Artist> findByActiveTrue();
    List<Artist> findByActiveTrueOrderByNameAsc();

    List<Artist> findBySpotifyUrlIsNotNull();
    List<Artist> findByYoutubeUrlIsNotNull();
    List<Artist> findBySoundcloudUrlIsNotNull();
    List<Artist> findByInstagramUrlIsNotNull();

    List<Artist> findByActiveTrueAndNameContainingIgnoreCase(String name);

    List<Artist> findTop10ByActiveTrueOrderByRegistrationDateDesc();

    @Query("SELECT a FROM Artist a WHERE a.id IN " +
            "(SELECT af.artist.id FROM ArtistFollower af WHERE af.user.id = :userId)")
    List<Artist> findArtistsFollowedByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(af) FROM ArtistFollower af WHERE af.artist.id = :artistId")
    Long countFollowersByArtistId(@Param("artistId") Long artistId);

    @Query("SELECT a FROM Artist a WHERE a.active = true ORDER BY " +
            "(SELECT COUNT(af) FROM ArtistFollower af WHERE af.artist.id = a.id) DESC")
    List<Artist> findMostPopularArtists();

    @Query("SELECT a FROM Artist a WHERE a.active = true AND " +
            "(a.spotifyUrl IS NOT NULL OR a.youtubeUrl IS NOT NULL OR a.soundcloudUrl IS NOT NULL)")
    List<Artist> findArtistsWithMusicPlatforms();

    @Query("SELECT a FROM Artist a JOIN ArtistGenre ag ON a.id = ag.artist.id " +
            "WHERE ag.genre.id = :genreId AND a.active = true")
    List<Artist> findByGenreId(@Param("genreId") Long genreId);
}