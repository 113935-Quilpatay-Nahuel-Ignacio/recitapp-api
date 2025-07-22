package com.recitapp.recitapp_api.modules.ticket.controller;

import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import com.recitapp.recitapp_api.modules.ticket.service.impl.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for managing ticket reservations
 */
@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Value("${recitapp.tickets.reservation.expiry-minutes:10}")
    private int reservationExpiryMinutes;

    /**
     * Endpoint to get all expired reservations
     *
     * @return A list of expired reservation IDs
     */
    @GetMapping("/expired")
    public ResponseEntity<List<Long>> getExpiredReservations() {
        List<Ticket> expiredReservations = reservationService.findExpiredReservations(reservationExpiryMinutes);
        List<Long> expiredReservationIds = expiredReservations.stream()
                .map(Ticket::getId)
                .toList();

        return ResponseEntity.ok(expiredReservationIds);
    }

    /**
     * Endpoint to manually clear expired reservations
     *
     * @return The number of cleared reservations
     */
    @DeleteMapping("/expired")
    public ResponseEntity<Map<String, Integer>> clearExpiredReservations() {
        int clearedCount = reservationService.clearExpiredReservations(reservationExpiryMinutes);
        return ResponseEntity.ok(Map.of("clearedCount", clearedCount));
    }

    /**
     * Endpoint to count reserved tickets for a specific event
     *
     * @param eventId The ID of the event
     * @return The count of reserved tickets
     */
    @GetMapping("/count/event/{eventId}")
    public ResponseEntity<Map<String, Long>> countReservedTickets(@PathVariable Long eventId) {
        Long count = reservationService.countReservedTicketsByEventId(eventId);
        return ResponseEntity.ok(Map.of("reservedTicketsCount", count));
    }
}