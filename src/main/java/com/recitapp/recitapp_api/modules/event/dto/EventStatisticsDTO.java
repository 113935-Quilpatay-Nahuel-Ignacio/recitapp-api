package com.recitapp.recitapp_api.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con estadísticas de un evento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStatisticsDTO {
    private Long eventId;
    private String eventName;
    private Long totalTickets;
    private Long soldTickets;
    private Double occupancyRate; // Tasa de ocupación (porcentaje)
    private String statusName;
}