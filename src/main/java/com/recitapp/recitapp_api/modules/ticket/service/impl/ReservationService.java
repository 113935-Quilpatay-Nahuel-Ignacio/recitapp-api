package com.recitapp.recitapp_api.modules.ticket.service.impl;

import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import com.recitapp.recitapp_api.modules.ticket.entity.TicketStatus;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketReservationRepository;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing ticket reservations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final TicketReservationRepository ticketReservationRepository;
    private final TicketStatusRepository ticketStatusRepository;

    /**
     * Finds all reserved tickets that have expired
     *
     * @param reservationExpiryMinutes The number of minutes after which a reservation expires
     * @return A list of expired reserved tickets
     */
    public List<Ticket> findExpiredReservations(int reservationExpiryMinutes) {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(reservationExpiryMinutes);
        return ticketReservationRepository.findExpiredReservations(expirationTime);
    }

    /**
     * Deletes all expired ticket reservations by setting their status back to 'DISPONIBLE'
     *
     * @param reservationExpiryMinutes The number of minutes after which a reservation expires
     * @return The number of reservations cleared
     */
    @Transactional
    public int clearExpiredReservations(int reservationExpiryMinutes) {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(reservationExpiryMinutes);

        // Get the 'DISPONIBLE' status
        TicketStatus availableStatus = ticketStatusRepository.findByName("DISPONIBLE")
                .orElseThrow(() -> new RuntimeException("Status 'DISPONIBLE' not found"));

        // Update all expired reservations
        int updatedCount = ticketReservationRepository.updateExpiredReservationsStatus(
                expirationTime, availableStatus.getId());

        log.info("Cleared {} expired ticket reservations", updatedCount);
        return updatedCount;
    }

    /**
     * Counts the number of reserved tickets for a given event
     *
     * @param eventId The ID of the event
     * @return The number of reserved tickets
     */
    public Long countReservedTicketsByEventId(Long eventId) {
        return ticketReservationRepository.countReservedTicketsByEventId(eventId);
    }
}