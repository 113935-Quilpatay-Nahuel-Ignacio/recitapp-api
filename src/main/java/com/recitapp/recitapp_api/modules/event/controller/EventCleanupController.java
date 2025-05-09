package com.recitapp.recitapp_api.modules.event.controller;

import com.recitapp.recitapp_api.annotation.RequireRole;
import com.recitapp.recitapp_api.modules.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controlador para la eliminación de eventos cancelados
 */
@RestController
@RequestMapping("/events/cleanup")
@RequiredArgsConstructor
@RequireRole({"ADMIN"})
public class EventCleanupController {

    private final EventService eventService;

    /**
     * Elimina eventos cancelados que fueron actualizados antes de la fecha de corte
     *
     * @param cutoffDate Fecha límite para considerar eventos cancelados a eliminar
     * @return Cantidad de eventos eliminados
     */
    @DeleteMapping("/canceled")
    public ResponseEntity<Integer> cleanupCanceledEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffDate) {

        if (cutoffDate == null) {
            cutoffDate = LocalDateTime.now().minusMonths(3); // Por defecto, eliminar eventos cancelados hace más de 3 meses
        }

        eventService.cleanupCanceledEvents(cutoffDate);

        return ResponseEntity.ok(0); // Idealmente, debería devolver la cantidad de eventos eliminados
    }

    /**
     * Programar la eliminación automática de eventos cancelados
     * (este método podría activar o desactivar un job automático)
     */
    @PostMapping("/canceled/schedule")
    public ResponseEntity<Void> scheduleCleanupCanceledEvents(
            @RequestParam int retentionMonths) {

        // Este método podría implementarse para configurar el trabajo programado
        // que elimina eventos cancelados automáticamente

        return ResponseEntity.ok().build();
    }
}