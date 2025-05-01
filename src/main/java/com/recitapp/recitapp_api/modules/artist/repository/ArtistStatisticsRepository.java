package com.recitapp.recitapp_api.modules.artist.repository;

import com.recitapp.recitapp_api.modules.artist.entity.ArtistStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtistStatisticsRepository extends JpaRepository<ArtistStatistics, Long> {

    Optional<ArtistStatistics> findByArtistId(Long artistId);

    @Query("SELECT stats FROM ArtistStatistics stats ORDER BY stats.totalFollowers DESC")
    List<ArtistStatistics> findTopArtistsByFollowersCount(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(f) FROM ArtistFollower f WHERE f.artist.id = :artistId")
    Long countFollowersForArtist(@Param("artistId") Long artistId);

    @Query("SELECT COUNT(e) FROM Event e JOIN EventArtist ea ON e.id = ea.event.id " +
            "WHERE ea.artist.id = :artistId")
    Long countEventsForArtist(@Param("artistId") Long artistId);

    @Query("SELECT COALESCE(SUM(stats.totalEvents), 0) FROM ArtistStatistics stats")
    Long getTotalEventsCount();

    @Query("SELECT COALESCE(SUM(stats.totalFollowers), 0) FROM ArtistStatistics stats")
    Long getTotalFollowersCount();

    @Query(value = "SELECT artist_id FROM artist_statistics " +
            "WHERE update_date >= :startDate AND update_date <= :endDate " +
            "ORDER BY total_followers DESC LIMIT :limit", nativeQuery = true)
    List<Long> findTrendingArtists(@Param("startDate") java.sql.Timestamp startDate,
                                   @Param("endDate") java.sql.Timestamp endDate,
                                   @Param("limit") int limit);
}