package com.recitapp.recitapp_api.modules.event.repository;

import com.recitapp.recitapp_api.modules.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

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

    @Query("SELECT e FROM Event e WHERE e.venue.id = :venueId AND e.startDateTime > :now")
    List<Event> findUpcomingByVenueId(@Param("venueId") Long venueId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.venue.id = :venueId")
    Integer countByVenueId(@Param("venueId") Long venueId);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.venue.id = :venueId AND e.startDateTime > :now")
    Integer countUpcomingByVenueId(@Param("venueId") Long venueId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.venue.id = :venueId AND e.startDateTime <= :now")
    Integer countPastByVenueId(@Param("venueId") Long venueId, @Param("now") LocalDateTime now);

    // Buscar eventos con filtros opcionales
    @Query("SELECT e FROM Event e WHERE " +
            "(:startDate IS NULL OR e.startDateTime >= :startDate) AND " +
            "(:endDate IS NULL OR e.startDateTime <= :endDate) AND " +
            "(:venueId IS NULL OR e.venue.id = :venueId) AND " +
            "(:statusName IS NULL OR e.status.name = :statusName) AND " +
            "(:artistId IS NULL OR e.mainArtist.id = :artistId OR " +
            "EXISTS (SELECT ea FROM EventArtist ea WHERE ea.event.id = e.id AND ea.artist.id = :artistId))")
    List<Event> findByFilters(@Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              @Param("venueId") Long venueId,
                              @Param("artistId") Long artistId,
                              @Param("statusName") String statusName);

    // Buscar eventos por venue
    List<Event> findByVenueId(Long venueId);

    // Buscar eventos en un rango de fechas
    @Query("SELECT e FROM Event e WHERE e.startDateTime >= :startDate AND e.startDateTime <= :endDate")
    List<Event> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    // Buscar eventos próximos (desde ahora)
    @Query("SELECT e FROM Event e WHERE e.startDateTime > :now ORDER BY e.startDateTime")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now);

    // Buscar eventos por artista
    @Query("SELECT e FROM Event e JOIN EventArtist ea ON e.id = ea.event.id WHERE ea.artist.id = :artistId")
    List<Event> findByArtistId(@Param("artistId") Long artistId);

    // Buscar eventos donde el artista es el artista principal
    List<Event> findByMainArtistId(Long artistId);

    // Buscar eventos futuros donde el artista es el artista principal
    List<Event> findByMainArtistIdAndStartDateTimeAfter(Long artistId, LocalDateTime startDateTime);

    // Verificar si existe un evento solapado en el mismo venue
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.venue.id = :venueId " +
            "AND ((e.startDateTime <= :endDateTime AND e.endDateTime >= :startDateTime) " +
            "OR (e.startDateTime >= :startDateTime AND e.startDateTime <= :endDateTime) " +
            "OR (e.endDateTime >= :startDateTime AND e.endDateTime <= :endDateTime)) " +
            "AND e.status.name <> 'CANCELADO'")
    boolean existsOverlappingEvent(@Param("venueId") Long venueId,
                                   @Param("startDateTime") LocalDateTime startDateTime,
                                   @Param("endDateTime") LocalDateTime endDateTime);

    // Buscar eventos con filtros opcionales
    @Query("SELECT e FROM Event e WHERE " +
            "(:startDate IS NULL OR e.startDateTime >= :startDate) AND " +
            "(:endDate IS NULL OR e.startDateTime <= :endDate) AND " +
            "(:venueId IS NULL OR e.venue.id = :venueId) AND " +
            "(:statusName IS NULL OR e.status.name = :statusName) AND " +
            "(:artistId IS NULL OR e.mainArtist.id = :artistId OR " +
            "EXISTS (SELECT ea FROM EventArtist ea WHERE ea.event.id = e.id AND ea.artist.id = :artistId)) AND " +
            "(:verified IS NULL OR e.verified = :verified) AND " +
            "(:moderatorId IS NULL OR e.moderator.id = :moderatorId) AND " +
            "(:registrarId IS NULL OR e.registrar.id = :registrarId)")
    List<Event> findByFilters(@Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              @Param("venueId") Long venueId,
                              @Param("artistId") Long artistId,
                              @Param("statusName") String statusName,
                              @Param("verified") Boolean verified,
                              @Param("moderatorId") Long moderatorId,
                              @Param("registrarId") Long registrarId);

    // Buscar eventos cancelados antes de una fecha
    @Query("SELECT e FROM Event e WHERE e.status.name = 'CANCELADO' AND e.updatedAt < :cutoffDate")
    List<Event> findCanceledEventsBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}