package com.recitapp.recitapp_api.modules.event.controller;

import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventStatusUpdateRequest;
import com.recitapp.recitapp_api.modules.event.entity.EventStatus;
import com.recitapp.recitapp_api.modules.event.repository.EventStatusRepository;
import com.recitapp.recitapp_api.modules.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de estados de eventos
 */
@RestController
@RequestMapping("/events")  // Define base para todos los métodos
@RequiredArgsConstructor
public class EventStatusController {

    private final EventService eventService;
    private final EventStatusRepository eventStatusRepository;

    /**
     * Obtiene todos los estados de eventos disponibles
     */
    @GetMapping("/statuses")  // Resulta en: /events/statuses
    public ResponseEntity<List<String>> getAllEventStatuses() {
        List<String> statuses = eventStatusRepository.findAll().stream()
                .map(EventStatus::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(statuses);
    }

    /**
     * Actualiza el estado de un evento
     */
    @PatchMapping("/{eventId}/status")  // Resulta en: /events/{eventId}/status
    public ResponseEntity<EventDTO> updateEventStatus(
            @PathVariable Long eventId,
            @Valid @RequestBody EventStatusUpdateRequest request) {

        EventDTO updatedEvent = eventService.updateEventStatus(eventId, request.getStatusName());
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * Marca un evento como cancelado
     */
    @PatchMapping("/{eventId}/cancel")  // Resulta en: /events/{eventId}/cancel
    public ResponseEntity<Void> cancelEvent(
            @PathVariable Long eventId,
            @RequestParam(required = false) String cancellationReason) {

        eventService.cancelEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marca eventos pasados como finalizados
     */
    @PatchMapping("/batch/finalize-past")  // Resulta en: /events/batch/finalize-past
    public ResponseEntity<Integer> finalizePastEvents() {
        int updatedEvents = 0; // eventService.finalizePastEvents();
        return ResponseEntity.ok(updatedEvents);
    }
}