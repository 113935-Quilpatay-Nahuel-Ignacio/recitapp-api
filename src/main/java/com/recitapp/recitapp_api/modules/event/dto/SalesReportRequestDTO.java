package com.recitapp.recitapp_api.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * DTO for sales report requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportRequestDTO {

    private Long eventId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private Long venueId;

    private Long artistId;

    private String reportType;

    private boolean includePromotionalTickets = true;

    private boolean includeCanceledTickets = false;

    private boolean groupBySection = true;
}