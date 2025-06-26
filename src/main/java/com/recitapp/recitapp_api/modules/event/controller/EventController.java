package com.recitapp.recitapp_api.modules.event.controller;

import com.recitapp.recitapp_api.annotation.RequireRole;
import com.recitapp.recitapp_api.modules.common.service.FileStorageService;
import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventDetailDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventFilterDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventStatisticsDTO;
import com.recitapp.recitapp_api.modules.event.service.EventService;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    /**
     * Obtiene el usuario actual autenticado
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            return userRepository.findByEmail(email)
                    .orElse(null);
        }
        return null;
    }

    /**
     * Verifica si el usuario actual puede ver eventos no verificados
     */
    private boolean canViewUnverifiedEvents(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String roleName = user.getRole().getName();
        return "ADMIN".equals(roleName) || "MODERADOR".equals(roleName) || "REGISTRADOR_EVENTO".equals(roleName);
    }

    /**
     * Verifica si el usuario actual puede editar/eliminar el evento (ADMIN, MODERADOR o propietario si es REGISTRADOR_EVENTO)
     */
    private boolean canModifyEvent(User user, Long eventId) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        
        String roleName = user.getRole().getName();
        
        // ADMIN y MODERADOR pueden modificar cualquier evento
        if ("ADMIN".equals(roleName) || "MODERADOR".equals(roleName)) {
            return true;
        }
        
        // REGISTRADOR_EVENTO solo puede modificar eventos que creó
        if ("REGISTRADOR_EVENTO".equals(roleName)) {
            try {
                EventDTO event = eventService.getEventForEdit(eventId);
                return event.getRegistrarId().equals(user.getId());
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }

    @PostMapping
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO eventDTO,
                                                @RequestParam Long registrarId) {
        EventDTO createdEvent = eventService.createEvent(eventDTO, registrarId);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @PostMapping("/with-files")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<EventDTO> createEventWithFiles(
            @RequestParam("eventData") String eventDataJson,
            @RequestParam("registrarId") Long registrarId,
            @RequestParam(value = "flyerImage", required = false) MultipartFile flyerImage,
            @RequestParam(value = "sectionsImage", required = false) MultipartFile sectionsImage) {
        
        try {
            // Aquí implementaremos la lógica para manejar archivos
            // Por ahora mantenemos compatibilidad con el método existente
            
            // TODO: Implementar parsing de JSON y manejo de archivos
            throw new RuntimeException("Endpoint en desarrollo");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id,
                                                @Valid @RequestBody EventDTO eventDTO) {
        
        User currentUser = getCurrentUser();
        if (!canModifyEvent(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        EventDTO updatedEvent = eventService.updateEvent(id, eventDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        
        User currentUser = getCurrentUser();
        if (!canModifyEvent(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventDetail(@PathVariable Long id) {
        EventDTO eventDetail = eventService.getEventDetailAsDTO(id);
        return ResponseEntity.ok(eventDetail);
    }

    @GetMapping("/{id}/edit")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
    public ResponseEntity<EventDTO> getEventForEdit(@PathVariable Long id) {
        
        User currentUser = getCurrentUser();
        if (!canModifyEvent(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        EventDTO event = eventService.getEventForEdit(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents(
            @RequestParam(required = false) Boolean upcomingOnly) {

        // Apply role-based filtering
        User currentUser = getCurrentUser();
        
        // If user can't view unverified events, we need to filter
        if (!canViewUnverifiedEvents(currentUser)) {
            EventFilterDTO filterDTO = new EventFilterDTO();
            filterDTO.setVerified(true);
            if (upcomingOnly != null && upcomingOnly) {
                filterDTO.setStartDate(LocalDateTime.now());
            }
            List<EventDTO> events = eventService.searchEvents(filterDTO);
            return ResponseEntity.ok(events);
        } else {
            // For privileged users, use the original method
            List<EventDTO> events = eventService.getAllEvents(upcomingOnly);
            return ResponseEntity.ok(events);
        }
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
        filterDTO.setModeratorId(moderatorId);
        filterDTO.setRegistrarId(registrarId);

        // Apply role-based filtering for event verification
        User currentUser = getCurrentUser();
        if (!canViewUnverifiedEvents(currentUser)) {
            // For COMPRADOR and other non-privileged roles, only show verified events
            filterDTO.setVerified(true);
        } else {
            // For ADMIN, MODERADOR, REGISTRADOR_EVENTO, use the requested filter or show all
            filterDTO.setVerified(verified);
        }

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