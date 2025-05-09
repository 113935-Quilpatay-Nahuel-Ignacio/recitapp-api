package com.recitapp.recitapp_api.modules.event.controller;

import com.recitapp.recitapp_api.annotation.RequireRole;
import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventDetailDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventFilterDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventVerificationRequest;
import com.recitapp.recitapp_api.modules.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador para la verificación de eventos propuestos
 */
@RestController
@RequestMapping("/events/verification")
@RequiredArgsConstructor
@RequireRole({"ADMIN", "MODERADOR"})
public class EventVerificationController {

    private final EventService eventService;

    /**
     * Obtiene eventos pendientes de verificación
     */
    @GetMapping("/pending")
    public ResponseEntity<List<EventDTO>> getPendingEvents() {
        // Crear filtro para eventos no verificados
        // En este caso, buscamos eventos que tengan el campo verified = false
        EventFilterDTO filter = new EventFilterDTO();
        filter.setVerified(false);

        List<EventDTO> pendingEvents = eventService.searchEvents(filter);
        return ResponseEntity.ok(pendingEvents);
    }

    /**
     * Obtiene el detalle de un evento pendiente de verificación
     */
    @GetMapping("/pending/{eventId}")
    public ResponseEntity<EventDetailDTO> getPendingEventDetail(@PathVariable Long eventId) {
        EventDetailDTO eventDetail = eventService.getEventDetail(eventId);

        // Verificar que el evento aún no está verificado
        if (eventDetail.getVerified()) {
            return ResponseEntity.badRequest()
                    .header("X-Error-Message", "Event is already verified")
                    .build();
        }

        return ResponseEntity.ok(eventDetail);
    }

    /**
     * Verifica un evento propuesto
     */
    @PostMapping("/{eventId}/verify")
    public ResponseEntity<EventDTO> verifyEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventVerificationRequest verificationRequest) {

        EventDTO verifiedEvent = eventService.verifyEventWithDetails(eventId, verificationRequest);
        return ResponseEntity.ok(verifiedEvent);
    }

    /**
     * Rechaza un evento propuesto (lo marca como cancelado)
     */
    @PostMapping("/{eventId}/reject")
    public ResponseEntity<Void> rejectEvent(
            @PathVariable Long eventId,
            @RequestParam(required = false) String rejectionReason) {

        // Cancelar el evento
        eventService.cancelEvent(eventId);

        // Aquí se podría implementar la lógica para guardar el motivo de rechazo
        // y notificar al creador del evento

        return ResponseEntity.noContent().build();
    }
}