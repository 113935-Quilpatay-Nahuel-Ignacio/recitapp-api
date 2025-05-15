package com.recitapp.recitapp_api.modules.event.controller;

import com.recitapp.recitapp_api.modules.venue.dto.SectionAvailabilityDTO;
import com.recitapp.recitapp_api.modules.event.service.EventAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventAvailabilityController {

    private final EventAvailabilityService eventAvailabilityService;

    @GetMapping("/{eventId}/sections/availability")
    public ResponseEntity<List<SectionAvailabilityDTO>> getSectionAvailability(@PathVariable Long eventId) {
        List<SectionAvailabilityDTO> availability = eventAvailabilityService.getEventSectionsAvailability(eventId);
        return ResponseEntity.ok(availability);
    }

    @GetMapping("/{eventId}/sections/{sectionId}/availability")
    public ResponseEntity<SectionAvailabilityDTO> getSectionAvailability(
            @PathVariable Long eventId, @PathVariable Long sectionId) {
        SectionAvailabilityDTO availability = eventAvailabilityService.getSectionAvailability(eventId, sectionId);
        return ResponseEntity.ok(availability);
    }
}