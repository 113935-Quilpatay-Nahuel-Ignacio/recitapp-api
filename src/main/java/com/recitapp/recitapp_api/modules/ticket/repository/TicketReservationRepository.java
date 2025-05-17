package com.recitapp.recitapp_api.modules.ticket.repository;

import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketReservationRepository extends JpaRepository<Ticket, Long> {

    /**
     * Finds all reserved tickets that have expired based on reservation time
     *
     * @param expirationTime The cutoff time for expired reservations
     * @return A list of expired reserved tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.status.name = 'RESERVADA' AND t.updatedAt < :expirationTime")
    List<Ticket> findExpiredReservations(@Param("expirationTime") LocalDateTime expirationTime);

    /**
     * Counts the number of reserved tickets for a given event
     *
     * @param eventId The ID of the event
     * @return The number of reserved tickets
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status.name = 'RESERVADA'")
    Long countReservedTicketsByEventId(@Param("eventId") Long eventId);

    /**
     * Updates the status of all expired reservations to 'DISPONIBLE'
     *
     * @param expirationTime The cutoff time for expired reservations
     * @param newStatusId The ID of the 'DISPONIBLE' status
     * @return The number of tickets updated
     */
    @Modifying
    @Query("UPDATE Ticket t SET t.status.id = :newStatusId, t.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE t.status.name = 'RESERVADA' AND t.updatedAt < :expirationTime")
    int updateExpiredReservationsStatus(
            @Param("expirationTime") LocalDateTime expirationTime,
            @Param("newStatusId") Long newStatusId);
}