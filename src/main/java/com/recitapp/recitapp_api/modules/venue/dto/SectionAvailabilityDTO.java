package com.recitapp.recitapp_api.modules.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionAvailabilityDTO {
    private Long sectionId;
    private String sectionName;
    private Integer totalCapacity;
    private Long availableTickets;
    private Long soldTickets;
    private Double availabilityPercentage;
    
    // Información de precios para este evento específico
    private List<TicketPriceInfo> ticketPrices;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketPriceInfo {
        private Long ticketPriceId;
        private String ticketType;
        private BigDecimal price;
        private Integer availableQuantity;
    }
}