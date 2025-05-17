package com.recitapp.recitapp_api.modules.ticket.scheduler;

import com.recitapp.recitapp_api.modules.ticket.service.impl.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for ticket reservation related tasks
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduler {

    private final ReservationService reservationService;

    @Value("${recitapp.tickets.reservation.expiry-minutes:10}")
    private int reservationExpiryMinutes;

    /**
     * Task that runs every minute to clear expired ticket reservations
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void clearExpiredReservations() {
        log.debug("Running scheduled task to clear expired reservations");
        int clearedCount = reservationService.clearExpiredReservations(reservationExpiryMinutes);

        if (clearedCount > 0) {
            log.info("Cleared {} expired ticket reservations", clearedCount);
        }
    }
}