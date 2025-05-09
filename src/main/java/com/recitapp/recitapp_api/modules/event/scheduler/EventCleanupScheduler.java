package com.recitapp.recitapp_api.modules.event.scheduler;

import com.recitapp.recitapp_api.modules.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Componente para ejecutar tareas programadas relacionadas con eventos
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventCleanupScheduler {

    private final EventService eventService;

    /**
     * Tarea programada para limpiar eventos cancelados antiguos
     * Se ejecuta el primer día de cada mes a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void cleanupCanceledEvents() {
        log.info("Starting scheduled cleanup of canceled events");

        // Eliminar eventos cancelados de hace más de 3 meses
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(3);

        try {
            eventService.cleanupCanceledEvents(cutoffDate);
            log.info("Completed cleanup of canceled events before {}", cutoffDate);
        } catch (Exception e) {
            log.error("Error during cleanup of canceled events", e);
        }
    }

    /**
     * Tarea programada para actualizar el estado de eventos finalizados
     * Se ejecuta todos los días a las 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void updateFinishedEvents() {
        log.info("Starting scheduled update of finished events");

        try {
            // Buscar eventos que ya ocurrieron, pero aún no están marcados como finalizados
            LocalDateTime now = LocalDateTime.now();

            // Esta implementación dependerá de cómo se haya estructurado el repositorio
            // y el servicio para actualizar eventos finalizados.
            // Por ahora, dejamos un comentario como placeholder.

            // List<Event> finishedEvents = eventRepository.findEventsThatShouldBeFinished(now);
            // for (Event event : finishedEvents) {
            //     eventService.updateEventStatus(event.getId(), "FINALIZADO");
            // }

            log.info("Completed update of finished events");
        } catch (Exception e) {
            log.error("Error during update of finished events", e);
        }
    }
}