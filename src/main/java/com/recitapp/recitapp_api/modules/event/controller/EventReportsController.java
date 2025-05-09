package com.recitapp.recitapp_api.modules.event.controller;

import com.recitapp.recitapp_api.annotation.RequireRole;
import com.recitapp.recitapp_api.modules.event.dto.EventFilterDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventStatisticsDTO;
import com.recitapp.recitapp_api.modules.event.service.EventReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la generación de informes y estadísticas de eventos
 */
@RestController
@RequestMapping("/events/reports")
@RequiredArgsConstructor
public class EventReportsController {

    private final EventReportService eventReportService;

    /**
     * Genera un informe de asistencia de eventos
     */
    @GetMapping("/attendance")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
    public ResponseEntity<Map<String, Object>> getAttendanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) String statusName) {

        Map<String, Object> report = eventReportService.generateAttendanceReport(startDate, endDate, venueId, statusName);
        return ResponseEntity.ok(report);
    }

    /**
     * Genera estadísticas de ventas de entradas para eventos
     */
    @GetMapping("/ticket-sales")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
    public ResponseEntity<Map<String, Object>> getTicketSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Long artistId) {

        Map<String, Object> report = eventReportService.generateTicketSalesReport(startDate, endDate, venueId, artistId);
        return ResponseEntity.ok(report);
    }

    /**
     * Obtiene estadísticas para múltiples eventos
     */
    @GetMapping("/statistics")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
    public ResponseEntity<List<EventStatisticsDTO>> getEventsStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) String statusName) {

        EventFilterDTO filterDTO = new EventFilterDTO();
        filterDTO.setStartDate(startDate);
        filterDTO.setEndDate(endDate);
        filterDTO.setVenueId(venueId);
        filterDTO.setArtistId(artistId);
        filterDTO.setStatusName(statusName);

        List<EventStatisticsDTO> statistics = eventReportService.getEventsStatistics(filterDTO);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Obtiene las estadísticas de los eventos más populares
     */
    @GetMapping("/popular")
    public ResponseEntity<List<EventStatisticsDTO>> getPopularEvents(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<EventStatisticsDTO> popularEvents = eventReportService.getPopularEvents(limit, startDate, endDate);
        return ResponseEntity.ok(popularEvents);
    }
}