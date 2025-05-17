package com.recitapp.recitapp_api.modules.ticket.repository;

import com.recitapp.recitapp_api.modules.ticket.entity.TicketVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ticket verification entities
 */
@Repository
public interface TicketVerificationRepository extends JpaRepository<TicketVerification, Long> {

    /**
     * Finds all verifications for a specific ticket
     *
     * @param ticketId The ID of the ticket
     * @return A list of ticket verifications
     */
    List<TicketVerification> findByTicketId(Long ticketId);

    /**
     * Finds successful verifications for a specific ticket
     *
     * @param ticketId The ID of the ticket
     * @return A list of successful ticket verifications
     */
    List<TicketVerification> findByTicketIdAndSuccessfulTrue(Long ticketId);

    /**
     * Checks if a ticket has been successfully verified before
     *
     * @param ticketId The ID of the ticket
     * @return true if the ticket has been successfully verified, false otherwise
     */
    boolean existsByTicketIdAndSuccessfulTrue(Long ticketId);

    /**
     * Finds the most recent verification for a ticket
     *
     * @param ticketId The ID of the ticket
     * @return The most recent ticket verification
     */
    TicketVerification findFirstByTicketIdOrderByVerificationTimeDesc(Long ticketId);

    /**
     * Counts verifications for a specific event on a specific date
     *
     * @param eventId The ID of the event
     * @param startDate The start date
     * @param endDate The end date
     * @return The count of verifications
     */
    @Query("SELECT COUNT(tv) FROM TicketVerification tv WHERE tv.event.id = :eventId " +
            "AND tv.verificationTime BETWEEN :startDate AND :endDate AND tv.successful = true")
    Long countByEventIdAndVerificationTimeBetween(
            @Param("eventId") Long eventId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}