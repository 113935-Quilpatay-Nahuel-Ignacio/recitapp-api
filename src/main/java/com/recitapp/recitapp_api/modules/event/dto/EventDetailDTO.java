package com.recitapp.recitapp_api.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con información detallada de un evento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long venueId;
    private String venueName;
    private Long mainArtistId;
    private String mainArtistName;
    private String statusName;
    private String flyerImage;
    private String sectionsImage;

    private Boolean verified;
    private LocalDateTime salesStartDate;
    private LocalDateTime salesEndDate;
    private LocalDateTime registrationDate;
    private LocalDateTime updatedAt;

    // IDs para verificación de permisos
    private Long moderatorId;
    private Long registrarId;

    // IDs de artistas adicionales
    private List<Long> artistIds;

    // Estadísticas
    private Long totalTickets;
    private Long soldTickets;
}