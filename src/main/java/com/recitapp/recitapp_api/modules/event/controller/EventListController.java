package com.recitapp.recitapp_api.modules.event.controller;

import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventFilterDTO;
import com.recitapp.recitapp_api.modules.event.service.EventService;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para emitir listados de eventos según diferentes criterios
 */
@RestController
@RequestMapping("/events/lists")
@RequiredArgsConstructor
public class EventListController {

    private final EventService eventService;
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
     * Aplica filtros de verificación según el rol del usuario
     */
    private EventFilterDTO applyRoleBasedFilters(EventFilterDTO filter) {
        User currentUser = getCurrentUser();
        if (!canViewUnverifiedEvents(currentUser)) {
            filter.setVerified(true);
        }
        return filter;
    }

    /**
     * Emite un listado de eventos por fecha
     *
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de eventos en el rango de fechas
     */
    @GetMapping("/by-date")
    public ResponseEntity<List<EventDTO>> getEventsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Si no se proporciona fecha de fin, usar un mes después de la fecha de inicio
        if (endDate == null) {
            endDate = startDate.plusMonths(1);
        }

        List<EventDTO> events = eventService.getEventsByDateRange(startDate, endDate);
        return ResponseEntity.ok(events);
    }

    /**
     * Emite un listado de eventos por ubicación (venue)
     *
     * @param venueId ID del recinto
     * @param upcomingOnly Si es true, solo devuelve eventos futuros
     * @return Lista de eventos en el recinto especificado
     */
    @GetMapping("/by-venue/{venueId}")
    public ResponseEntity<List<EventDTO>> getEventsByVenue(
            @PathVariable Long venueId,
            @RequestParam(required = false, defaultValue = "true") Boolean upcomingOnly) {

        List<EventDTO> events;

        if (upcomingOnly) {
            // Filtrar solo eventos futuros
            EventFilterDTO filter = new EventFilterDTO();
            filter.setStartDate(LocalDateTime.now());
            filter.setVenueId(venueId);
            events = eventService.searchEvents(filter);
        } else {
            // Todos los eventos del venue
            events = eventService.getEventsByVenue(venueId);
        }

        return ResponseEntity.ok(events);
    }

    /**
     * Emite un listado de eventos por artista
     *
     * @param artistId ID del artista
     * @param upcomingOnly Si es true, solo devuelve eventos futuros
     * @return Lista de eventos del artista especificado
     */
    @GetMapping("/by-artist/{artistId}")
    public ResponseEntity<List<EventDTO>> getEventsByArtist(
            @PathVariable Long artistId,
            @RequestParam(required = false, defaultValue = "true") Boolean upcomingOnly) {

        List<EventDTO> events;

        if (upcomingOnly) {
            // Filtrar solo eventos futuros
            EventFilterDTO filter = new EventFilterDTO();
            filter.setStartDate(LocalDateTime.now());
            filter.setArtistId(artistId);
            events = eventService.searchEvents(filter);
        } else {
            // Todos los eventos del artista
            events = eventService.getEventsByArtist(artistId);
        }

        return ResponseEntity.ok(events);
    }

    /**
     * Emite un listado de eventos por estado
     *
     * @param statusName Nombre del estado
     * @return Lista de eventos con el estado especificado
     */
    @GetMapping("/by-status/{statusName}")
    public ResponseEntity<List<EventDTO>> getEventsByStatus(
            @PathVariable String statusName) {

        EventFilterDTO filter = new EventFilterDTO();
        filter.setStatusName(statusName);

        List<EventDTO> events = eventService.searchEvents(filter);
        return ResponseEntity.ok(events);
    }
}