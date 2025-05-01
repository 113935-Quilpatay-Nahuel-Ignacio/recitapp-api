package com.recitapp.recitapp_api.modules.event.repository;

import com.recitapp.recitapp_api.modules.event.entity.EventArtist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventArtistRepository extends JpaRepository<EventArtist, EventArtist.EventArtistId> {

    List<EventArtist> findByArtistId(Long artistId);

    @Query("SELECT ea FROM EventArtist ea JOIN ea.event e WHERE ea.artist.id = :artistId AND e.startDateTime > :now")
    List<EventArtist> findUpcomingEventsByArtistId(@Param("artistId") Long artistId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(ea) FROM EventArtist ea JOIN ea.event e WHERE ea.artist.id = :artistId AND e.startDateTime > :now")
    Long countUpcomingEventsByArtistId(@Param("artistId") Long artistId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(ea) FROM EventArtist ea JOIN ea.event e WHERE ea.artist.id = :artistId AND e.startDateTime <= :now")
    Long countPastEventsByArtistId(@Param("artistId") Long artistId, @Param("now") LocalDateTime now);
}