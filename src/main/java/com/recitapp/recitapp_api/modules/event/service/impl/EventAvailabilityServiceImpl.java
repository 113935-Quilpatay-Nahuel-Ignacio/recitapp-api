package com.recitapp.recitapp_api.modules.event.service.impl;

import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.entity.TicketPrice;
import com.recitapp.recitapp_api.modules.event.repository.EventRepository;
import com.recitapp.recitapp_api.modules.event.repository.TicketPriceRepository;
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
    private final TicketPriceRepository ticketPriceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SectionAvailabilityDTO> getEventSectionsAvailability(Long eventId) {
        // Validate event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        // Get all sections for the venue that have ticket prices for this event
        List<VenueSection> sections = venueSectionRepository.findByVenueId(event.getVenue().getId());

        // Calculate availability for each section that has prices defined for this event
        return sections.stream()
                .map(section -> calculateSectionAvailability(event.getId(), section))
                .filter(sectionAvailability -> sectionAvailability.getTicketPrices() != null && !sectionAvailability.getTicketPrices().isEmpty())
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

        // Get ticket prices for this event and section
        List<TicketPrice> ticketPrices = ticketPriceRepository.findByEventIdAndSectionId(eventId, section.getId());
        
        // Calculate available tickets based on ticket prices (not just section capacity)
        Long availableTickets = ticketPrices.stream()
                .mapToLong(tp -> tp.getAvailableQuantity())
                .sum() - soldTickets;

        // Calculate availability percentage based on total available tickets from prices
        Long totalAvailableFromPrices = ticketPrices.stream()
                .mapToLong(tp -> tp.getAvailableQuantity())
                .sum();
        
        Double availabilityPercentage = totalAvailableFromPrices > 0
                ? (availableTickets.doubleValue() / totalAvailableFromPrices.doubleValue()) * 100
                : 0.0;

        // Convert ticket prices to DTO format
        List<SectionAvailabilityDTO.TicketPriceInfo> ticketPriceInfos = ticketPrices.stream()
                .map(tp -> SectionAvailabilityDTO.TicketPriceInfo.builder()
                        .ticketPriceId(tp.getId())
                        .ticketType(tp.getTicketType())
                        .price(tp.getPrice())
                        .availableQuantity(tp.getAvailableQuantity())
                        .build())
                .collect(Collectors.toList());

        return SectionAvailabilityDTO.builder()
                .sectionId(section.getId())
                .sectionName(section.getName())
                .totalCapacity(totalCapacity)
                .availableTickets(availableTickets)
                .soldTickets(soldTickets)
                .availabilityPercentage(availabilityPercentage)
                .ticketPrices(ticketPriceInfos)
                .build();
    }
}