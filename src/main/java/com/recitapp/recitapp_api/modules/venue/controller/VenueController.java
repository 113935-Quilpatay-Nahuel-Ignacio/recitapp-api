package com.recitapp.recitapp_api.modules.venue.controller;

import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.venue.dto.*;
import com.recitapp.recitapp_api.modules.venue.service.VenueService;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueDTO> createVenue(@Valid @RequestBody VenueDTO venueDTO) {
        VenueDTO createdVenue = venueService.createVenue(venueDTO);
        return new ResponseEntity<>(createdVenue, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueDTO> updateVenue(@PathVariable Long id, @Valid @RequestBody VenueUpdateDTO venueDTO) {
        VenueDTO updatedVenue = venueService.updateVenue(id, venueDTO);
        return ResponseEntity.ok(updatedVenue);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueDTO> deactivateVenue(@PathVariable Long id) {
        VenueDTO deactivatedVenue = venueService.deactivateVenue(id);
        return ResponseEntity.ok(deactivatedVenue);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueDTO> activateVenue(@PathVariable Long id) {
        VenueDTO activatedVenue = venueService.activateVenue(id);
        return ResponseEntity.ok(activatedVenue);
    }

    @GetMapping("/available")
    public ResponseEntity<List<VenueDTO>> getAvailableVenues(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime) {
        List<VenueDTO> availableVenues = venueService.getAvailableVenues(startDateTime, endDateTime);
        return ResponseEntity.ok(availableVenues);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<VenueAvailabilityDTO> checkVenueAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime) {
        VenueAvailabilityDTO availability = venueService.checkVenueAvailability(id, startDateTime, endDateTime);
        return ResponseEntity.ok(availability);
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<EventDTO>> getVenueEvents(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") Boolean includePastEvents) {
        User currentUser = getCurrentUser();
        boolean canViewUnverified = canViewUnverifiedEvents(currentUser);
        List<EventDTO> events = venueService.getVenueEvents(id, includePastEvents, canViewUnverified);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<VenueStatisticsDTO> getVenueStatistics(@PathVariable Long id) {
        VenueStatisticsDTO statistics = venueService.getVenueStatistics(id);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics")
    public ResponseEntity<List<VenueStatisticsDTO>> getVenuesStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<VenueStatisticsDTO> statistics = venueService.getVenuesStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/{venueId}/sections")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueSectionDTO> createVenueSection(
            @PathVariable Long venueId,
            @Valid @RequestBody VenueSectionDTO sectionDTO) {
        VenueSectionDTO createdSection = venueService.createVenueSection(venueId, sectionDTO);
        return new ResponseEntity<>(createdSection, HttpStatus.CREATED);
    }

    @PutMapping("/{venueId}/sections/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueSectionDTO> updateVenueSection(
            @PathVariable Long venueId,
            @PathVariable Long sectionId,
            @Valid @RequestBody VenueSectionDTO sectionDTO) {
        VenueSectionDTO updatedSection = venueService.updateVenueSection(venueId, sectionId, sectionDTO);
        return ResponseEntity.ok(updatedSection);
    }

    @DeleteMapping("/{venueId}/sections/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVenueSection(
            @PathVariable Long venueId,
            @PathVariable Long sectionId) {
        venueService.deleteVenueSection(venueId, sectionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{venueId}/sections")
    public ResponseEntity<List<VenueSectionDTO>> getVenueSections(@PathVariable Long venueId) {
        List<VenueSectionDTO> sections = venueService.getVenueSections(venueId);
        return ResponseEntity.ok(sections);
    }

    @PatchMapping("/{id}/location")
    public ResponseEntity<VenueDTO> updateVenueLocation(
            @PathVariable Long id,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        VenueDTO updatedVenue = venueService.updateVenueLocation(id, latitude, longitude);
        return ResponseEntity.ok(updatedVenue);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<VenueDTO>> findVenuesNearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusInKm) {
        List<VenueDTO> nearbyVenues = venueService.findVenuesNearby(latitude, longitude, radiusInKm);
        return ResponseEntity.ok(nearbyVenues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueDTO> getVenueById(@PathVariable Long id) {
        VenueDTO venue = venueService.getVenueById(id);
        return ResponseEntity.ok(venue);
    }

    @GetMapping
    public ResponseEntity<List<VenueDTO>> getAllVenues(
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            @RequestParam(required = false) String name) {

        if (name != null && !name.isEmpty()) {
            List<VenueDTO> venues = venueService.searchVenuesByName(name);
            return ResponseEntity.ok(venues);
        } else {
            List<VenueDTO> venues = venueService.getAllVenues(activeOnly);
            return ResponseEntity.ok(venues);
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<VenueDTO>> getVenuesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VenueDTO> venues = venueService.getVenuesPaginated(pageable, search, activeOnly);
        return ResponseEntity.ok(venues);
    }
}