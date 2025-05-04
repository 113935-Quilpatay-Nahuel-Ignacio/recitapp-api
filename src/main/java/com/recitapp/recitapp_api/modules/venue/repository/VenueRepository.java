package com.recitapp.recitapp_api.modules.venue.repository;

import com.recitapp.recitapp_api.modules.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    boolean existsByName(String name);

    Optional<Venue> findByName(String name);

    List<Venue> findByActiveTrue();

    @Query("SELECT v FROM Venue v WHERE v.id NOT IN " +
            "(SELECT DISTINCT e.venue.id FROM Event e WHERE " +
            "(:startDateTime BETWEEN e.startDateTime AND e.endDateTime OR " +
            ":endDateTime BETWEEN e.startDateTime AND e.endDateTime OR " +
            "e.startDateTime BETWEEN :startDateTime AND :endDateTime) AND e.status.name <> 'CANCELADO')")
    List<Venue> findAvailableVenues(@Param("startDateTime") LocalDateTime startDateTime,
                                    @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT v, COUNT(e) as eventCount FROM Venue v " +
            "LEFT JOIN Event e ON v.id = e.venue.id " +
            "WHERE (:periodStart IS NULL OR e.startDateTime >= :periodStart) AND " +
            "(:periodEnd IS NULL OR e.startDateTime <= :periodEnd) " +
            "GROUP BY v.id ORDER BY eventCount DESC")
    List<Object[]> findVenuesWithEventCount(@Param("periodStart") LocalDateTime periodStart,
                                            @Param("periodEnd") LocalDateTime periodEnd);

    @Query(value = "SELECT * FROM venues v WHERE " +
            "ST_Distance_Sphere(point(v.longitude, v.latitude), point(:lng, :lat)) <= :radiusMeters " +
            "AND v.active = true", nativeQuery = true)
    List<Venue> findVenuesNearby(@Param("lat") double latitude,
                                 @Param("lng") double longitude,
                                 @Param("radiusMeters") double radiusInMeters);
}