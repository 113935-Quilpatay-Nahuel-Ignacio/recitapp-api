package com.recitapp.recitapp_api.modules.ticket.repository;

import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import com.recitapp.recitapp_api.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUserId(Long userId);

    List<Ticket> findByEventId(Long eventId);

    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId AND t.section.id = :sectionId")
    List<Ticket> findByEventIdAndSectionId(@Param("eventId") Long eventId, @Param("sectionId") Long sectionId);

    @Query("SELECT t FROM Ticket t WHERE t.qrCode = :qrCode")
    Optional<Ticket> findByQrCode(@Param("qrCode") String qrCode);

    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId AND t.status.name = 'ACTIVO'")
    List<Ticket> findActiveTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT t FROM Ticket t WHERE t.user.id = :userId AND t.status.name = 'ACTIVO'")
    List<Ticket> findActiveTicketsByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Ticket t WHERE t.event.startDateTime BETWEEN :startDate AND :endDate")
    List<Ticket> findTicketsByEventDateRange(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status.name = 'ACTIVO'")
    Long countActiveTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.section.id = :sectionId AND t.status.name = 'ACTIVO'")
    Long countActiveTicketsByEventAndSection(@Param("eventId") Long eventId, @Param("sectionId") Long sectionId);

    @Query("SELECT vs.id, vs.name, COUNT(t) as ticketCount " +
           "FROM VenueSection vs " +
           "LEFT JOIN Ticket t ON vs.id = t.section.id AND t.event.id = :eventId AND t.status.name = 'ACTIVO' " +
           "WHERE vs.venue.id = (SELECT e.venue.id FROM Event e WHERE e.id = :eventId) " +
           "GROUP BY vs.id, vs.name " +
           "ORDER BY vs.name")
    List<Object[]> getTicketCountBySectionForEvent(@Param("eventId") Long eventId);

    @Query("SELECT t.status.name, COUNT(t) FROM Ticket t GROUP BY t.status.name")
    List<Object[]> getTicketStatisticsByStatus();

    Page<Ticket> findByUserOrderByPurchaseDateDesc(User user, Pageable pageable);

    Optional<Ticket> findByIdentificationCode(String identificationCode);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.section.id = :sectionId AND t.status.name = :statusName")
    Long countByEventIdAndSectionIdAndStatusName(
            @Param("eventId") Long eventId,
            @Param("sectionId") Long sectionId,
            @Param("statusName") String statusName);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status.name = 'VENDIDA'")
    Long countSoldTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT t FROM Ticket t " +
           "WHERE t.status.name = 'VENDIDA' " +
           "AND t.event.endDateTime < :now")
    List<Ticket> findExpiredSoldTickets(@Param("now") LocalDateTime now);
}