package com.recitapp.recitapp_api.modules.event.service.impl;

import com.recitapp.recitapp_api.modules.event.dto.EventFilterDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventStatisticsDTO;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.repository.EventRepository;
import com.recitapp.recitapp_api.modules.event.repository.EventStatisticsRepository;
import com.recitapp.recitapp_api.modules.event.service.EventReportService;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventReportServiceImpl implements EventReportService {

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final EventStatisticsRepository eventStatisticsRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateAttendanceReport(LocalDateTime startDate, LocalDateTime endDate,
                                                        Long venueId, String statusName) {
        // Si no se proporciona fecha de inicio, usar la fecha actual
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1); // Por defecto, el último mes
        }

        // Si no se proporciona fecha de fin, usar un mes después de la fecha de inicio
        if (endDate == null) {
            endDate = LocalDateTime.now().plusMonths(1); // Por defecto, el próximo mes
        }

        // Filtrar eventos según los criterios
        EventFilterDTO filterDTO = new EventFilterDTO();
        filterDTO.setStartDate(startDate);
        filterDTO.setEndDate(endDate);
        filterDTO.setVenueId(venueId);
        filterDTO.setStatusName(statusName);

        List<Event> events = eventRepository.findByFilters(
                startDate, endDate, venueId, null, statusName);

        // Preparar el informe
        Map<String, Object> report = new HashMap<>();
        report.put("reportDate", LocalDateTime.now());
        report.put("startDate", startDate);
        report.put("endDate", endDate);

        // Total de eventos y tickets
        int totalEvents = events.size();
        long totalTickets = 0;
        long soldTickets = 0;

        // Estadísticas por estado
        Map<String, Integer> eventsByStatus = new HashMap<>();

        // Estadísticas por venue
        Map<String, Integer> eventsByVenue = new HashMap<>();

        // Recopila datos para cada evento
        List<Map<String, Object>> eventsData = new ArrayList<>();

        for (Event event : events) {
            // Contar tickets
            long eventTickets = ticketRepository.countByEventId(event.getId());
            totalTickets += eventTickets;

            long eventSoldTickets = ticketRepository.countSoldTicketsByEventId(event.getId());
            soldTickets += eventSoldTickets;

            // Estadísticas por estado
            String status = event.getStatus().getName();
            eventsByStatus.put(status, eventsByStatus.getOrDefault(status, 0) + 1);

            // Estadísticas por venue
            String venueName = event.getVenue().getName();
            eventsByVenue.put(venueName, eventsByVenue.getOrDefault(venueName, 0) + 1);

            // Datos del evento
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("id", event.getId());
            eventData.put("name", event.getName());
            eventData.put("venue", event.getVenue().getName());
            eventData.put("status", status);
            eventData.put("startDateTime", event.getStartDateTime());
            eventData.put("totalTickets", eventTickets);
            eventData.put("soldTickets", eventSoldTickets);
            eventData.put("occupancyRate", eventTickets > 0 ? (eventSoldTickets * 100.0) / eventTickets : 0);

            eventsData.add(eventData);
        }

        // Añadir totales y estadísticas al informe
        report.put("totalEvents", totalEvents);
        report.put("totalTickets", totalTickets);
        report.put("soldTickets", soldTickets);
        report.put("occupancyRate", totalTickets > 0 ? (soldTickets * 100.0) / totalTickets : 0);
        report.put("eventsByStatus", eventsByStatus);
        report.put("eventsByVenue", eventsByVenue);
        report.put("events", eventsData);

        // Formatear fechas para lectura humana
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        report.put("formattedStartDate", startDate.format(formatter));
        report.put("formattedEndDate", endDate.format(formatter));
        report.put("formattedReportDate", LocalDateTime.now().format(formatter));

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateTicketSalesReport(LocalDateTime startDate, LocalDateTime endDate,
                                                         Long venueId, Long artistId) {
        // Si no se proporciona fecha de inicio, usar la fecha actual
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1); // Por defecto, el último mes
        }

        // Si no se proporciona fecha de fin, usar un mes después de la fecha de inicio
        if (endDate == null) {
            endDate = LocalDateTime.now().plusMonths(1); // Por defecto, el próximo mes
        }

        // Filtrar eventos según los criterios
        List<Event> events = eventRepository.findByFilters(
                startDate, endDate, venueId, artistId, null);

        // Preparar el informe
        Map<String, Object> report = new HashMap<>();
        report.put("reportDate", LocalDateTime.now());
        report.put("startDate", startDate);
        report.put("endDate", endDate);

        // Total de eventos y tickets
        int totalEvents = events.size();
        long totalTickets = 0;
        long soldTickets = 0;

        // Ventas por evento
        Map<String, Long> salesByEvent = new HashMap<>();

        // Ventas por venue
        Map<String, Long> salesByVenue = new HashMap<>();

        // Ventas por estado
        Map<String, Long> salesByStatus = new HashMap<>();

        // Ventas por artista (si aplica)
        Map<String, Long> salesByArtist = new HashMap<>();

        // Recopila datos para cada evento
        List<Map<String, Object>> eventsData = new ArrayList<>();

        for (Event event : events) {
            // Contar tickets
            long eventTickets = ticketRepository.countByEventId(event.getId());
            totalTickets += eventTickets;

            long eventSoldTickets = ticketRepository.countSoldTicketsByEventId(event.getId());
            soldTickets += eventSoldTickets;

            // Ventas por evento
            salesByEvent.put(event.getName(), eventSoldTickets);

            // Ventas por venue
            String venueName = event.getVenue().getName();
            salesByVenue.put(venueName, salesByVenue.getOrDefault(venueName, 0L) + eventSoldTickets);

            // Ventas por estado
            String status = event.getStatus().getName();
            salesByStatus.put(status, salesByStatus.getOrDefault(status, 0L) + eventSoldTickets);

            // Ventas por artista principal
            if (event.getMainArtist() != null) {
                String artistName = event.getMainArtist().getName();
                salesByArtist.put(artistName, salesByArtist.getOrDefault(artistName, 0L) + eventSoldTickets);
            }

            // Datos del evento
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("id", event.getId());
            eventData.put("name", event.getName());
            eventData.put("venue", event.getVenue().getName());
            eventData.put("status", status);
            eventData.put("startDateTime", event.getStartDateTime());
            eventData.put("totalTickets", eventTickets);
            eventData.put("soldTickets", eventSoldTickets);
            eventData.put("occupancyRate", eventTickets > 0 ? (eventSoldTickets * 100.0) / eventTickets : 0);

            eventsData.add(eventData);
        }

        // Añadir totales y estadísticas al informe
        report.put("totalEvents", totalEvents);
        report.put("totalTickets", totalTickets);
        report.put("soldTickets", soldTickets);
        report.put("salesByEvent", salesByEvent);
        report.put("salesByVenue", salesByVenue);
        report.put("salesByStatus", salesByStatus);
        report.put("salesByArtist", salesByArtist);
        report.put("events", eventsData);

        // Formatear fechas para lectura humana
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        report.put("formattedStartDate", startDate.format(formatter));
        report.put("formattedEndDate", endDate.format(formatter));
        report.put("formattedReportDate", LocalDateTime.now().format(formatter));

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventStatisticsDTO> getEventsStatistics(EventFilterDTO filterDTO) {
        // Si no se proporciona fecha de inicio, usar la fecha actual
        if (filterDTO.getStartDate() == null) {
            filterDTO.setStartDate(LocalDateTime.now().minusMonths(1)); // Por defecto, el último mes
        }

        // Si no se proporciona fecha de fin, usar un mes después de la fecha de inicio
        if (filterDTO.getEndDate() == null) {
            filterDTO.setEndDate(LocalDateTime.now().plusMonths(1)); // Por defecto, el próximo mes
        }

        List<Event> events = eventRepository.findByFilters(
                filterDTO.getStartDate(),
                filterDTO.getEndDate(),
                filterDTO.getVenueId(),
                filterDTO.getArtistId(),
                filterDTO.getStatusName());

        // Convertir eventos a DTOs con estadísticas
        return events.stream()
                .map(event -> {
                    Long totalTickets = ticketRepository.countByEventId(event.getId());
                    Long soldTickets = ticketRepository.countSoldTicketsByEventId(event.getId());
                    Double occupancyRate = totalTickets > 0 ? (soldTickets * 100.0) / totalTickets : 0;

                    return EventStatisticsDTO.builder()
                            .eventId(event.getId())
                            .eventName(event.getName())
                            .totalTickets(totalTickets)
                            .soldTickets(soldTickets)
                            .occupancyRate(occupancyRate)
                            .statusName(event.getStatus().getName())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventStatisticsDTO> getPopularEvents(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        // Si no se proporciona fecha de inicio, usar la fecha actual
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(3); // Por defecto, los últimos 3 meses
        }

        // Si no se proporciona fecha de fin, usar la fecha actual
        if (endDate == null) {
            endDate = LocalDateTime.now().plusMonths(3); // Por defecto, los próximos 3 meses
        }

        // Obtener todos los eventos en el rango de fechas
        List<Event> events = eventRepository.findByDateRange(startDate, endDate);

        // Calcular estadísticas y ordenar por tickets vendidos
        List<EventStatisticsDTO> statistics = events.stream()
                .map(event -> {
                    Long totalTickets = ticketRepository.countByEventId(event.getId());
                    Long soldTickets = ticketRepository.countSoldTicketsByEventId(event.getId());
                    Double occupancyRate = totalTickets > 0 ? (soldTickets * 100.0) / totalTickets : 0;

                    return EventStatisticsDTO.builder()
                            .eventId(event.getId())
                            .eventName(event.getName())
                            .totalTickets(totalTickets)
                            .soldTickets(soldTickets)
                            .occupancyRate(occupancyRate)
                            .statusName(event.getStatus().getName())
                            .build();
                })
                .sorted(Comparator.comparing(EventStatisticsDTO::getSoldTickets).reversed())
                .collect(Collectors.toList());

        // Limitar la cantidad de resultados
        return statistics.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}