package com.recitapp.recitapp_api.modules.event.service.impl;

import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.artist.entity.Artist;
import com.recitapp.recitapp_api.modules.artist.repository.ArtistRepository;
import com.recitapp.recitapp_api.modules.event.dto.SalesReportRequestDTO;
import com.recitapp.recitapp_api.modules.event.dto.SalesReportResponseDTO;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.repository.EventRepository;
import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketRepository;
import com.recitapp.recitapp_api.modules.venue.entity.Venue;
import com.recitapp.recitapp_api.modules.venue.entity.VenueSection;
import com.recitapp.recitapp_api.modules.venue.repository.VenueRepository;
import com.recitapp.recitapp_api.modules.venue.repository.VenueSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating event sales reports
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalesReportService {

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final VenueRepository venueRepository;
    private final VenueSectionRepository venueSectionRepository;
    private final ArtistRepository artistRepository;

    /**
     * Generates a sales report based on the provided criteria
     *
     * @param requestDTO The report request parameters
     * @return A sales report
     */
    @Transactional(readOnly = true)
    public SalesReportResponseDTO generateSalesReport(SalesReportRequestDTO requestDTO) {
        // Initialize response builder
        SalesReportResponseDTO.SalesReportResponseDTOBuilder reportBuilder = SalesReportResponseDTO.builder()
                .reportType(requestDTO.getReportType())
                .generatedDate(LocalDateTime.now());

        // Set filter details
        setFilterDetails(reportBuilder, requestDTO);

        // Get all relevant tickets based on the filters
        List<Ticket> tickets = getFilteredTickets(requestDTO);

        // Calculate basic metrics
        calculateBasicMetrics(reportBuilder, tickets, requestDTO);

        // Generate section-based data if requested
        if (requestDTO.isGroupBySection()) {
            List<SalesReportResponseDTO.SectionSalesDTO> sectionSales = generateSectionSales(tickets, requestDTO);
            reportBuilder.sectionSales(sectionSales);
        }

        // Generate time-based data if this is a time report
        if ("TIME".equalsIgnoreCase(requestDTO.getReportType())) {
            List<SalesReportResponseDTO.TimeSegmentSalesDTO> timeSegmentSales =
                    generateTimeSegmentSales(tickets, requestDTO);
            reportBuilder.timeSegmentSales(timeSegmentSales);
        }

        // Generate status breakdown
        Map<String, Integer> statusCounts = generateStatusBreakdown(tickets);
        reportBuilder.ticketStatusCounts(statusCounts);

        return reportBuilder.build();
    }

    /**
     * Sets filter details in the report builder
     *
     * @param reportBuilder The report builder
     * @param requestDTO The request parameters
     */
    private void setFilterDetails(SalesReportResponseDTO.SalesReportResponseDTOBuilder reportBuilder,
                                  SalesReportRequestDTO requestDTO) {
        // Set time period
        LocalDateTime periodStartDate = requestDTO.getStartDate();
        if (periodStartDate == null) {
            // Default to 30 days ago
            periodStartDate = LocalDateTime.now().minusDays(30);
        }

        LocalDateTime periodEndDate = requestDTO.getEndDate();
        if (periodEndDate == null) {
            // Default to now
            periodEndDate = LocalDateTime.now();
        }

        reportBuilder.periodStartDate(periodStartDate);
        reportBuilder.periodEndDate(periodEndDate);

        // Set event details if specified
        if (requestDTO.getEventId() != null) {
            Event event = eventRepository.findById(requestDTO.getEventId())
                    .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + requestDTO.getEventId()));

            reportBuilder.eventId(event.getId());
            reportBuilder.eventName(event.getName());
        }

        // Set venue details if specified
        if (requestDTO.getVenueId() != null) {
            Venue venue = venueRepository.findById(requestDTO.getVenueId())
                    .orElseThrow(() -> new EntityNotFoundException("Venue not found with ID: " + requestDTO.getVenueId()));

            reportBuilder.venueId(venue.getId());
            reportBuilder.venueName(venue.getName());
        }

        // Set artist details if specified
        if (requestDTO.getArtistId() != null) {
            Artist artist = artistRepository.findById(requestDTO.getArtistId())
                    .orElseThrow(() -> new EntityNotFoundException("Artist not found with ID: " + requestDTO.getArtistId()));

            reportBuilder.artistId(artist.getId());
            reportBuilder.artistName(artist.getName());
        }
    }

    /**
     * Gets tickets based on the filter criteria
     *
     * @param requestDTO The request parameters
     * @return A list of filtered tickets
     */
    private List<Ticket> getFilteredTickets(SalesReportRequestDTO requestDTO) {
        // The implementation would depend on how your TicketRepository is structured
        // This is a simplified approach - in a real application, you'd want to do this filtering at the database level

        // Get all tickets within the date range
        List<Ticket> tickets;

        if (requestDTO.getEventId() != null) {
            // If specific event, get tickets for that event
            tickets = ticketRepository.findByEventId(requestDTO.getEventId());
        } else {
            // Otherwise, get all tickets and filter
            // This is not efficient - in a real app, you'd add repository methods for this
            tickets = ticketRepository.findAll();
        }

        // Further filtering logic
        LocalDateTime startDate = requestDTO.getStartDate() != null ? requestDTO.getStartDate() : LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = requestDTO.getEndDate() != null ? requestDTO.getEndDate() : LocalDateTime.now();

        return tickets.stream()
                .filter(ticket -> {
                    // Filter by purchase date
                    if (ticket.getPurchaseDate() == null) {
                        return false;
                    }

                    if (ticket.getPurchaseDate().isBefore(startDate) || ticket.getPurchaseDate().isAfter(endDate)) {
                        return false;
                    }

                    // Filter by venue
                    if (requestDTO.getVenueId() != null &&
                            !ticket.getEvent().getVenue().getId().equals(requestDTO.getVenueId())) {
                        return false;
                    }

                    // Filter by artist
                    if (requestDTO.getArtistId() != null) {
                        // Check if it's the main artist
                        boolean isMainArtist = ticket.getEvent().getMainArtist() != null &&
                                ticket.getEvent().getMainArtist().getId().equals(requestDTO.getArtistId());

                        // If not main artist, would need to check event artists
                        // For simplicity, we're just checking main artist here
                        if (!isMainArtist) {
                            return false;
                        }
                    }

                    // Filter by promotional status
                    if (!requestDTO.isIncludePromotionalTickets() && ticket.getIsGift()) {
                        return false;
                    }

                    // Filter by canceled status
                    if (!requestDTO.isIncludeCanceledTickets() && "CANCELADA".equals(ticket.getStatus().getName())) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculates basic report metrics
     *
     * @param reportBuilder The report builder
     * @param tickets The filtered tickets
     * @param requestDTO The request parameters
     */
    private void calculateBasicMetrics(SalesReportResponseDTO.SalesReportResponseDTOBuilder reportBuilder,
                                       List<Ticket> tickets,
                                       SalesReportRequestDTO requestDTO) {
        // Count total events
        Set<Long> uniqueEventIds = tickets.stream()
                .map(ticket -> ticket.getEvent().getId())
                .collect(Collectors.toSet());

        int totalEvents = uniqueEventIds.size();
        reportBuilder.totalEvents(totalEvents);

        // Count ticket statistics
        int totalTickets = tickets.size();
        reportBuilder.totalTickets(totalTickets);

        int soldTickets = (int) tickets.stream()
                .filter(ticket -> "VENDIDA".equals(ticket.getStatus().getName()) || "USADA".equals(ticket.getStatus().getName()))
                .count();
        reportBuilder.soldTickets(soldTickets);

        int promotionalTickets = (int) tickets.stream()
                .filter(Ticket::getIsGift)
                .count();
        reportBuilder.promotionalTickets(promotionalTickets);

        int canceledTickets = (int) tickets.stream()
                .filter(ticket -> "CANCELADA".equals(ticket.getStatus().getName()))
                .count();
        reportBuilder.canceledTickets(canceledTickets);

        // Calculate total revenue
        BigDecimal totalRevenue = tickets.stream()
                .filter(ticket -> "VENDIDA".equals(ticket.getStatus().getName()) || "USADA".equals(ticket.getStatus().getName()))
                .map(Ticket::getSalePrice)
                .filter(price -> price != null) // Filter out null prices (gift tickets)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        reportBuilder.totalRevenue(totalRevenue);

        // Calculate occupancy rate (if this is for a specific event)
        if (requestDTO.getEventId() != null) {
            Event event = eventRepository.findById(requestDTO.getEventId())
                    .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + requestDTO.getEventId()));

            int venueCapacity = event.getVenue().getTotalCapacity();
            double occupancyRate = venueCapacity > 0 ?
                    (double) soldTickets / venueCapacity * 100 : 0;

            reportBuilder.occupancyRate(occupancyRate);
        }
    }

    /**
     * Generates sales data by section
     *
     * @param tickets The filtered tickets
     * @param requestDTO The request parameters
     * @return A list of section sales data
     */
    private List<SalesReportResponseDTO.SectionSalesDTO> generateSectionSales(
            List<Ticket> tickets, SalesReportRequestDTO requestDTO) {

        // Group tickets by section
        Map<Long, List<Ticket>> ticketsBySection = tickets.stream()
                .collect(Collectors.groupingBy(ticket -> ticket.getSection().getId()));

        List<SalesReportResponseDTO.SectionSalesDTO> sectionSales = new ArrayList<>();

        // Create section sales data for each section
        for (Map.Entry<Long, List<Ticket>> entry : ticketsBySection.entrySet()) {
            Long sectionId = entry.getKey();
            List<Ticket> sectionTickets = entry.getValue();

            // Get section details
            VenueSection section = venueSectionRepository.findById(sectionId)
                    .orElseThrow(() -> new EntityNotFoundException("Section not found with ID: " + sectionId));

            // Count tickets by status
            int soldTickets = (int) sectionTickets.stream()
                    .filter(ticket -> "VENDIDA".equals(ticket.getStatus().getName()) || "USADA".equals(ticket.getStatus().getName()))
                    .count();

            int promotionalTickets = (int) sectionTickets.stream()
                    .filter(Ticket::getIsGift)
                    .count();

            int availableTickets = section.getCapacity() - soldTickets - promotionalTickets;

            // Calculate revenue
            BigDecimal totalRevenue = sectionTickets.stream()
                    .filter(ticket -> "VENDIDA".equals(ticket.getStatus().getName()) || "USADA".equals(ticket.getStatus().getName()))
                    .map(Ticket::getSalePrice)
                    .filter(price -> price != null) // Filter out null prices (gift tickets)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate occupancy rate
            double occupancyRate = section.getCapacity() > 0 ?
                    (double) (soldTickets + promotionalTickets) / section.getCapacity() * 100 : 0;

            // Create section sales DTO
            SalesReportResponseDTO.SectionSalesDTO sectionSalesDTO = SalesReportResponseDTO.SectionSalesDTO.builder()
                    .sectionId(sectionId)
                    .sectionName(section.getName())
                    .totalCapacity(section.getCapacity())
                    .soldTickets(soldTickets)
                    .promotionalTickets(promotionalTickets)
                    .availableTickets(availableTickets)
                    .totalRevenue(totalRevenue)
                    .occupancyRate(occupancyRate)
                    .build();

            sectionSales.add(sectionSalesDTO);
        }

        return sectionSales;
    }

    /**
     * Generates time segment sales data for time-based reports
     *
     * @param tickets The filtered tickets
     * @param requestDTO The request parameters
     * @return A list of time segment sales data
     */
    private List<SalesReportResponseDTO.TimeSegmentSalesDTO> generateTimeSegmentSales(
            List<Ticket> tickets, SalesReportRequestDTO requestDTO) {

        // Determine time period
        LocalDateTime startDate = requestDTO.getStartDate() != null ?
                requestDTO.getStartDate() : LocalDateTime.now().minusDays(30);

        LocalDateTime endDate = requestDTO.getEndDate() != null ?
                requestDTO.getEndDate() : LocalDateTime.now();

        // Determine appropriate time segments based on period length
        long daysBetween = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());

        // Choose segment size based on period length
        int segmentSizeHours;
        if (daysBetween <= 1) {
            // Less than a day: hourly segments
            segmentSizeHours = 1;
        } else if (daysBetween <= 7) {
            // Less than a week: 6-hour segments
            segmentSizeHours = 6;
        } else if (daysBetween <= 30) {
            // Less than a month: daily segments
            segmentSizeHours = 24;
        } else if (daysBetween <= 90) {
            // Less than 3 months: weekly segments
            segmentSizeHours = 24 * 7;
        } else {
            // Longer period: monthly segments
            segmentSizeHours = 24 * 30;
        }

        // Create time segments
        List<SalesReportResponseDTO.TimeSegmentSalesDTO> timeSegments = new ArrayList<>();

        LocalDateTime segmentStart = startDate;
        while (segmentStart.isBefore(endDate)) {
            LocalDateTime segmentEnd = segmentStart.plusHours(segmentSizeHours);
            if (segmentEnd.isAfter(endDate)) {
                segmentEnd = endDate;
            }

            // Get tickets for this segment
            final LocalDateTime finalSegmentStart = segmentStart;
            final LocalDateTime finalSegmentEnd = segmentEnd;

            List<Ticket> segmentTickets = tickets.stream()
                    .filter(ticket -> {
                        LocalDateTime purchaseDate = ticket.getPurchaseDate();
                        return purchaseDate != null &&
                                !purchaseDate.isBefore(finalSegmentStart) &&
                                purchaseDate.isBefore(finalSegmentEnd);
                    })
                    .collect(Collectors.toList());

            // Count sold tickets
            int ticketsSold = (int) segmentTickets.stream()
                    .filter(ticket -> "VENDIDA".equals(ticket.getStatus().getName()) || "USADA".equals(ticket.getStatus().getName()))
                    .count();

            // Calculate revenue
            BigDecimal revenue = segmentTickets.stream()
                    .filter(ticket -> "VENDIDA".equals(ticket.getStatus().getName()) || "USADA".equals(ticket.getStatus().getName()))
                    .map(Ticket::getSalePrice)
                    .filter(price -> price != null) // Filter out null prices (gift tickets)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Create time segment DTO
            SalesReportResponseDTO.TimeSegmentSalesDTO timeSegmentDTO = SalesReportResponseDTO.TimeSegmentSalesDTO.builder()
                    .segmentStart(segmentStart)
                    .segmentEnd(segmentEnd)
                    .ticketsSold(ticketsSold)
                    .revenue(revenue)
                    .build();

            timeSegments.add(timeSegmentDTO);

            // Move to next segment
            segmentStart = segmentEnd;
        }

        return timeSegments;
    }

    /**
     * Generates a breakdown of tickets by status
     *
     * @param tickets The filtered tickets
     * @return A map of status names to ticket counts
     */
    private Map<String, Integer> generateStatusBreakdown(List<Ticket> tickets) {
        return tickets.stream()
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getStatus().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    /**
     * Generates a sales report for a specific event
     *
     * @param eventId The ID of the event
     * @return A sales report
     */
    @Transactional(readOnly = true)
    public SalesReportResponseDTO generateEventSalesReport(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found with ID: " + eventId);
        }

        SalesReportRequestDTO requestDTO = new SalesReportRequestDTO();
        requestDTO.setEventId(eventId);
        requestDTO.setReportType("EVENT");
        requestDTO.setGroupBySection(true);
        requestDTO.setIncludePromotionalTickets(true);

        return generateSalesReport(requestDTO);
    }

    /**
     * Generates a sales report for a specific venue
     *
     * @param venueId The ID of the venue
     * @param startDate The start date
     * @param endDate The end date
     * @return A sales report
     */
    @Transactional(readOnly = true)
    public SalesReportResponseDTO generateVenueSalesReport(Long venueId, LocalDateTime startDate, LocalDateTime endDate) {
        if (!venueRepository.existsById(venueId)) {
            throw new EntityNotFoundException("Venue not found with ID: " + venueId);
        }

        SalesReportRequestDTO requestDTO = new SalesReportRequestDTO();
        requestDTO.setVenueId(venueId);
        requestDTO.setReportType("VENUE");
        requestDTO.setStartDate(startDate);
        requestDTO.setEndDate(endDate);
        requestDTO.setGroupBySection(true);

        return generateSalesReport(requestDTO);
    }

    /**
     * Generates a sales report for a specific artist
     *
     * @param artistId The ID of the artist
     * @param startDate The start date
     * @param endDate The end date
     * @return A sales report
     */
    @Transactional(readOnly = true)
    public SalesReportResponseDTO generateArtistSalesReport(Long artistId, LocalDateTime startDate, LocalDateTime endDate) {
        if (!artistRepository.existsById(artistId)) {
            throw new EntityNotFoundException("Artist not found with ID: " + artistId);
        }

        SalesReportRequestDTO requestDTO = new SalesReportRequestDTO();
        requestDTO.setArtistId(artistId);
        requestDTO.setReportType("ARTIST");
        requestDTO.setStartDate(startDate);
        requestDTO.setEndDate(endDate);

        return generateSalesReport(requestDTO);
    }

    /**
     * Generates a time-based sales report
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return A sales report
     */
    @Transactional(readOnly = true)
    public SalesReportResponseDTO generateTimeSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        SalesReportRequestDTO requestDTO = new SalesReportRequestDTO();
        requestDTO.setReportType("TIME");
        requestDTO.setStartDate(startDate);
        requestDTO.setEndDate(endDate);

        return generateSalesReport(requestDTO);
    }
}