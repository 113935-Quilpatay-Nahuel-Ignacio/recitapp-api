package com.recitapp.recitapp_api.modules.event.service.impl;

import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.repository.EventRepository;
import com.recitapp.recitapp_api.modules.event.service.EventAvailabilityService;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketRepository;
import com.recitapp.recitapp_api.modules.venue.dto.SectionAvailabilityDTO;
import com.recitapp.recitapp_api.modules.venue.entity.VenueSection;
import com.recitapp.recitapp_api.modules.venue.repository.VenueSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventAvailabilityServiceImpl implements EventAvailabilityService {

    private final EventRepository eventRepository;
    private final VenueSectionRepository venueSectionRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SectionAvailabilityDTO> getEventSectionsAvailability(Long eventId) {
        // Validate event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        // Get all sections for the venue
        List<VenueSection> sections = venueSectionRepository.findByVenueId(event.getVenue().getId());

        // Calculate availability for each section
        return sections.stream()
                .map(section -> calculateSectionAvailability(event.getId(), section))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SectionAvailabilityDTO getSectionAvailability(Long eventId, Long sectionId) {
        // Validate event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        // Validate section exists
        VenueSection section = venueSectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with ID: " + sectionId));

        // Validate section belongs to the event's venue
        if (!section.getVenue().getId().equals(event.getVenue().getId())) {
            throw new EntityNotFoundException("Section with ID " + sectionId +
                    " does not belong to the venue for event with ID " + eventId);
        }

        // Calculate availability for the section
        return calculateSectionAvailability(eventId, section);
    }

    private SectionAvailabilityDTO calculateSectionAvailability(Long eventId, VenueSection section) {
        // Get total capacity for the section
        Integer totalCapacity = section.getCapacity();

        // Count sold tickets for this event and section
        Long soldTickets = ticketRepository.countByEventIdAndSectionIdAndStatusName(
                eventId, section.getId(), "VENDIDA");

        // Calculate available tickets
        Long availableTickets = totalCapacity - soldTickets;

        // Calculate availability percentage
        Double availabilityPercentage = totalCapacity > 0
                ? (availableTickets.doubleValue() / totalCapacity.doubleValue()) * 100
                : 0.0;

        return SectionAvailabilityDTO.builder()
                .sectionId(section.getId())
                .sectionName(section.getName())
                .totalCapacity(totalCapacity)
                .availableTickets(availableTickets)
                .soldTickets(soldTickets)
                .basePrice(section.getBasePrice())
                .availabilityPercentage(availabilityPercentage)
                .build();
    }
}