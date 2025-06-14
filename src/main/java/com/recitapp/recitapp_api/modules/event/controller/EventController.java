package com.recitapp.recitapp_api.modules.event.controller;

import com.recitapp.recitapp_api.annotation.RequireRole;
import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventDetailDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventFilterDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventStatisticsDTO;
import com.recitapp.recitapp_api.modules.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO eventDTO,
                                                @RequestParam Long registrarId) {
        EventDTO createdEvent = eventService.createEvent(eventDTO, registrarId);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id,
                                                @Valid @RequestBody EventDTO eventDTO) {
        EventDTO updatedEvent = eventService.updateEvent(id, eventDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailDTO> getEventDetail(@PathVariable Long id) {
        EventDetailDTO eventDetail = eventService.getEventDetail(id);
        return ResponseEntity.ok(eventDetail);
    }

    @GetMapping("/{id}/edit")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<EventDTO> getEventForEdit(@PathVariable Long id) {
        EventDTO event = eventService.getEventForEdit(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents(
            @RequestParam(required = false) Boolean upcomingOnly) {
        List<EventDTO> events = eventService.getAllEvents(upcomingOnly);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventDTO>> searchEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) String statusName,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) Long moderatorId,
            @RequestParam(required = false) Long registrarId) {

        EventFilterDTO filterDTO = new EventFilterDTO();
        filterDTO.setStartDate(startDate);
        filterDTO.setEndDate(endDate);
        filterDTO.setVenueId(venueId);
        filterDTO.setArtistId(artistId);
        filterDTO.setStatusName(statusName);
        filterDTO.setVerified(verified);
        filterDTO.setModeratorId(moderatorId);
        filterDTO.setRegistrarId(registrarId);

        List<EventDTO> events = eventService.searchEvents(filterDTO);
        return ResponseEntity.ok(events);
    }

    @PatchMapping("/{id}/verify")
    @RequireRole({"ADMIN", "MODERADOR"})
    public ResponseEntity<EventDTO> verifyEvent(@PathVariable Long id,
                                                @RequestParam Long moderatorId) {
        EventDTO verifiedEvent = eventService.verifyEvent(id, moderatorId);
        return ResponseEntity.ok(verifiedEvent);
    }

    @PatchMapping("/{id}/status-simple")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
    public ResponseEntity<EventDTO> updateEventStatus(@PathVariable Long id,
                                                      @RequestParam String statusName) {
        EventDTO updatedEvent = eventService.updateEventStatus(id, statusName);
        return ResponseEntity.ok(updatedEvent);
    }

    @PatchMapping("/{id}/cancel-event")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
    public ResponseEntity<Void> cancelEvent(@PathVariable Long id) {
        eventService.cancelEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<List<EventDTO>> getEventsByVenue(@PathVariable Long venueId) {
        List<EventDTO> events = eventService.getEventsByVenue(venueId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<EventDTO>> getEventsByArtist(@PathVariable Long artistId) {
        List<EventDTO> events = eventService.getEventsByArtist(artistId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<EventDTO>> getEventsByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<EventDTO> events = eventService.getEventsByDateRange(startDate, endDate);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}/statistics")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
    public ResponseEntity<EventStatisticsDTO> getEventStatistics(@PathVariable Long id) {
        EventStatisticsDTO statistics = eventService.getEventStatistics(id);
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/cleanup-canceled")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> cleanupCanceledEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffDate) {
        if (cutoffDate == null) {
            cutoffDate = LocalDateTime.now().minusMonths(3); // Por defecto, eliminar eventos cancelados de hace más de 3 meses
        }
        eventService.cleanupCanceledEvents(cutoffDate);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/is-sold-out")
    public ResponseEntity<Boolean> isEventSoldOut(@PathVariable Long id) {
        Boolean isSoldOut = eventService.isEventSoldOut(id);
        return ResponseEntity.ok(isSoldOut);
    }
}