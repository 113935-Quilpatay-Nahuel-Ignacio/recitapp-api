package com.recitapp.recitapp_api.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for sales report responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportResponseDTO {

    private String reportType;
    private LocalDateTime generatedDate;
    private LocalDateTime periodStartDate;
    private LocalDateTime periodEndDate;

    // Summary data
    private int totalEvents;
    private int totalTickets;
    private int soldTickets;
    private int promotionalTickets;
    private int canceledTickets;
    private BigDecimal totalRevenue;
    private double occupancyRate;

    // Filter data
    private Long eventId;
    private String eventName;
    private Long venueId;
    private String venueName;
    private Long artistId;
    private String artistName;

    // Detailed data by section
    private List<SectionSalesDTO> sectionSales;

    // Ticket status breakdown
    private Map<String, Integer> ticketStatusCounts;

    // Sales timeline data (for time-based reports)
    private List<TimeSegmentSalesDTO> timeSegmentSales;

    /**
     * DTO for section sales data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionSalesDTO {
        private Long sectionId;
        private String sectionName;
        private int totalCapacity;
        private int soldTickets;
        private int promotionalTickets;
        private int availableTickets;
        private BigDecimal totalRevenue;
        private double occupancyRate;
    }

    /**
     * DTO for time segment sales data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSegmentSalesDTO {
        private LocalDateTime segmentStart;
        private LocalDateTime segmentEnd;
        private int ticketsSold;
        private BigDecimal revenue;
    }
}