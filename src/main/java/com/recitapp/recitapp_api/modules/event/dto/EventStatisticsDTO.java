package com.recitapp.recitapp_api.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<SectionStatisticsDTO> sectionStatistics; // Estadísticas por sección
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionStatisticsDTO {
        private Long sectionId;
        private String sectionName;
        private Long totalTicketsForSale; // Total de tickets disponibles para la venta
        private Long ticketsSold; // Tickets vendidos
        private Long ticketsRemaining; // Tickets restantes
        private Double percentageAvailable; // Porcentaje disponible
    }
}