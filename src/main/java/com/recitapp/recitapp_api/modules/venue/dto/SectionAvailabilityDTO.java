package com.recitapp.recitapp_api.modules.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    private BigDecimal basePrice;
    private Double availabilityPercentage;
}