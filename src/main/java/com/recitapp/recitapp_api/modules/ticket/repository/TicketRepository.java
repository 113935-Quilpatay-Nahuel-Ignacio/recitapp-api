package com.recitapp.recitapp_api.modules.ticket.repository;

import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Find tickets by event ID
    List<Ticket> findByEventId(Long eventId);

    // Find tickets by user ID
    List<Ticket> findByUserId(Long userId);

    // Find tickets by event ID and section ID
    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId AND t.section.id = :sectionId")
    List<Ticket> findByEventIdAndSectionId(@Param("eventId") Long eventId, @Param("sectionId") Long sectionId);

    // Count tickets by event ID, section ID, and status name
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.section.id = :sectionId AND t.status.name = :statusName")
    Long countByEventIdAndSectionIdAndStatusName(
            @Param("eventId") Long eventId,
            @Param("sectionId") Long sectionId,
            @Param("statusName") String statusName);

    // Count tickets by event ID
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);

    // Count sold tickets by event ID
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status.name = 'VENDIDA'")
    Long countSoldTicketsByEventId(@Param("eventId") Long eventId);

    // Find by identification code
    Optional<Ticket> findByIdentificationCode(String identificationCode);
}