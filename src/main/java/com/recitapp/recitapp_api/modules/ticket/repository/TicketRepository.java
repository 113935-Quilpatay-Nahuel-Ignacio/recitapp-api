package com.recitapp.recitapp_api.modules.ticket.repository;

import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Contar tickets por evento
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);

    // Contar tickets vendidos por evento
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status.name = 'VENDIDA'")
    Long countSoldTicketsByEventId(@Param("eventId") Long eventId);

    // Obtener tickets por evento
    List<Ticket> findByEventId(Long eventId);

    // Obtener tickets por usuario
    List<Ticket> findByUserId(Long userId);

    // Obtener tickets por evento y estado
    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId AND t.status.name = :statusName")
    List<Ticket> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("statusName") String statusName);

    // Contar tickets por evento y sección
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.section.id = :sectionId")
    Long countByEventIdAndSectionId(@Param("eventId") Long eventId, @Param("sectionId") Long sectionId);

    // Contar tickets vendidos por evento y sección
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.section.id = :sectionId AND t.status.name = 'VENDIDA'")
    Long countSoldTicketsByEventIdAndSectionId(@Param("eventId") Long eventId, @Param("sectionId") Long sectionId);
}