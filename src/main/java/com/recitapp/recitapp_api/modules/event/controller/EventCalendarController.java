package com.recitapp.recitapp_api.modules.event.controller;

import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventFilterDTO;
import com.recitapp.recitapp_api.modules.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para proporcionar eventos en formato de calendario
 */
@RestController
@RequestMapping("/events/calendar")
@RequiredArgsConstructor
public class EventCalendarController {

    private final EventService eventService;

    /**
     * Obtiene eventos para mostrar en un calendario mensual
     *
     * @param year Año del calendario
     * @param month Mes del calendario (1-12)
     * @param venueId ID opcional del recinto
     * @param artistId ID opcional del artista
     * @return Mapa con eventos agrupados por día
     */
    @GetMapping("/monthly")
    public ResponseEntity<Map<String, List<EventDTO>>> getMonthlyCalendar(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Long artistId) {

        // Calcular primer y último día del mes
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        // Convertir a LocalDateTime (inicio y fin del día)
        LocalDateTime startDateTime = LocalDateTime.of(startOfMonth, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(endOfMonth, LocalTime.MAX);

        // Crear filtro para búsqueda de eventos
        EventFilterDTO filter = new EventFilterDTO();
        filter.setStartDate(startDateTime);
        filter.setEndDate(endDateTime);
        filter.setVenueId(venueId);
        filter.setArtistId(artistId);

        // Buscar eventos
        List<EventDTO> events = eventService.searchEvents(filter);

        // Agrupar eventos por día (formato: "YYYY-MM-DD")
        Map<String, List<EventDTO>> eventsByDay = events.stream()
                .collect(Collectors.groupingBy(
                        event -> event.getStartDateTime().toLocalDate().toString(),
                        Collectors.toList()));

        return ResponseEntity.ok(eventsByDay);
    }

    /**
     * Obtiene eventos para mostrar en un calendario semanal
     *
     * @param year Año del calendario
     * @param weekNumber Número de la semana (1-53)
     * @param venueId ID opcional del recinto
     * @param artistId ID opcional del artista
     * @return Mapa con eventos agrupados por día
     */
    @GetMapping("/weekly")
    public ResponseEntity<Map<String, List<EventDTO>>> getWeeklyCalendar(
            @RequestParam int year,
            @RequestParam int weekNumber,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Long artistId) {

        // Calcular primer día de la semana (asumiendo que la semana comienza el lunes)
        LocalDate startOfWeek = LocalDate.ofYearDay(year, 1)
                .plusWeeks(weekNumber - 1)
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        // Calcular último día de la semana (domingo)
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // Convertir a LocalDateTime (inicio y fin del día)
        LocalDateTime startDateTime = LocalDateTime.of(startOfWeek, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(endOfWeek, LocalTime.MAX);

        // Crear filtro para búsqueda de eventos
        EventFilterDTO filter = new EventFilterDTO();
        filter.setStartDate(startDateTime);
        filter.setEndDate(endDateTime);
        filter.setVenueId(venueId);
        filter.setArtistId(artistId);

        // Buscar eventos
        List<EventDTO> events = eventService.searchEvents(filter);

        // Agrupar eventos por día (formato: "YYYY-MM-DD")
        Map<String, List<EventDTO>> eventsByDay = events.stream()
                .collect(Collectors.groupingBy(
                        event -> event.getStartDateTime().toLocalDate().toString(),
                        Collectors.toList()));

        return ResponseEntity.ok(eventsByDay);
    }

    /**
     * Obtiene eventos para mostrar en un calendario diario
     *
     * @param date Fecha del calendario (formato: YYYY-MM-DD)
     * @param venueId ID opcional del recinto
     * @param artistId ID opcional del artista
     * @return Lista de eventos para el día especificado
     */
    @GetMapping("/daily")
    public ResponseEntity<List<EventDTO>> getDailyCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Long artistId) {

        // Convertir a LocalDateTime (inicio y fin del día)
        LocalDateTime startDateTime = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(date, LocalTime.MAX);

        // Crear filtro para búsqueda de eventos
        EventFilterDTO filter = new EventFilterDTO();
        filter.setStartDate(startDateTime);
        filter.setEndDate(endDateTime);
        filter.setVenueId(venueId);
        filter.setArtistId(artistId);

        // Buscar eventos
        List<EventDTO> events = eventService.searchEvents(filter);

        return ResponseEntity.ok(events);
    }
}