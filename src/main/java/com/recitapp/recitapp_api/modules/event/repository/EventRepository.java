package com.recitapp.recitapp_api.modules.event.repository;

import com.recitapp.recitapp_api.modules.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e JOIN EventArtist ea ON e.id = ea.event.id WHERE ea.artist.id = :artistId")
    List<Event> findByArtistId(@Param("artistId") Long artistId);

    @Query("SELECT e FROM Event e JOIN EventArtist ea ON e.id = ea.event.id WHERE ea.artist.id = :artistId AND e.startDateTime > :now")
    List<Event> findUpcomingByArtistId(@Param("artistId") Long artistId, @Param("now") LocalDateTime now);

    @Query("SELECT e FROM Event e WHERE e.venue.id = :venueId AND " +
            "((e.startDateTime <= :endDateTime AND e.endDateTime >= :startDateTime) OR " +
            "(e.startDateTime >= :startDateTime AND e.startDateTime <= :endDateTime) OR " +
            "(e.endDateTime >= :startDateTime AND e.endDateTime <= :endDateTime)) AND " +
            "e.status.name <> 'CANCELADO'")
    List<Event> findConflictingEvents(@Param("venueId") Long venueId,
                                      @Param("startDateTime") LocalDateTime startDateTime,
                                      @Param("endDateTime") LocalDateTime endDateTime);

    List<Event> findByVenueId(Long venueId);

    @Query("SELECT e FROM Event e WHERE e.venue.id = :venueId AND e.startDateTime > :now")
    List<Event> findUpcomingByVenueId(@Param("venueId") Long venueId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.venue.id = :venueId")
    Integer countByVenueId(@Param("venueId") Long venueId);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.venue.id = :venueId AND e.startDateTime > :now")
    Integer countUpcomingByVenueId(@Param("venueId") Long venueId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.venue.id = :venueId AND e.startDateTime <= :now")
    Integer countPastByVenueId(@Param("venueId") Long venueId, @Param("now") LocalDateTime now);
}