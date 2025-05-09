package com.recitapp.recitapp_api.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para filtrar eventos por diferentes criterios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFilterDTO {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long venueId;
    private Long artistId;
    private String statusName;
    private Boolean verified;
    private Long moderatorId;
    private Long registrarId;
}