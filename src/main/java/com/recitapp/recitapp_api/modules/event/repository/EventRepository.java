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
}