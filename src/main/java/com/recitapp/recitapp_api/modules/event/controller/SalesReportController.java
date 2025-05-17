package com.recitapp.recitapp_api.modules.event.controller;

import com.recitapp.recitapp_api.annotation.RequireRole;
import com.recitapp.recitapp_api.modules.event.dto.SalesReportRequestDTO;
import com.recitapp.recitapp_api.modules.event.dto.SalesReportResponseDTO;
import com.recitapp.recitapp_api.modules.event.service.impl.SalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for sales reports
 */
@RestController
@RequestMapping("/reports/sales")
@RequiredArgsConstructor
@RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
public class SalesReportController {

    private final SalesReportService salesReportService;

    /**
     * Generates a custom sales report based on provided criteria
     *
     * @param requestDTO The report request parameters
     * @return The sales report
     */
    @PostMapping
    public ResponseEntity<SalesReportResponseDTO> generateSalesReport(
            @RequestBody SalesReportRequestDTO requestDTO) {

        SalesReportResponseDTO report = salesReportService.generateSalesReport(requestDTO);
        return ResponseEntity.ok(report);
    }

    /**
     * Generates a sales report for a specific event
     *
     * @param eventId The ID of the event
     * @return The sales report
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<SalesReportResponseDTO> generateEventSalesReport(
            @PathVariable Long eventId) {

        SalesReportResponseDTO report = salesReportService.generateEventSalesReport(eventId);
        return ResponseEntity.ok(report);
    }

    /**
     * Generates a sales report for a specific venue
     *
     * @param venueId The ID of the venue
     * @param startDate The start date (optional)
     * @param endDate The end date (optional)
     * @return The sales report
     */
    @GetMapping("/venue/{venueId}")
    public ResponseEntity<SalesReportResponseDTO> generateVenueSalesReport(
            @PathVariable Long venueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        SalesReportResponseDTO report = salesReportService.generateVenueSalesReport(venueId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Generates a sales report for a specific artist
     *
     * @param artistId The ID of the artist
     * @param startDate The start date (optional)
     * @param endDate The end date (optional)
     * @return The sales report
     */
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<SalesReportResponseDTO> generateArtistSalesReport(
            @PathVariable Long artistId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        SalesReportResponseDTO report = salesReportService.generateArtistSalesReport(artistId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Generates a time-based sales report
     *
     * @param startDate The start date (optional)
     * @param endDate The end date (optional)
     * @return The sales report
     */
    @GetMapping("/time")
    public ResponseEntity<SalesReportResponseDTO> generateTimeSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        SalesReportResponseDTO report = salesReportService.generateTimeSalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}