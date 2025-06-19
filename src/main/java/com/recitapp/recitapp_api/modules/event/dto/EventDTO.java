package com.recitapp.recitapp_api.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para transferencia de datos de eventos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private Long id;

    @NotBlank(message = "El nombre del evento es obligatorio")
    private String name;

    private String description;

    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    private LocalDateTime startDateTime;

    @NotNull(message = "La fecha y hora de fin son obligatorias")
    private LocalDateTime endDateTime;

    @NotNull(message = "El ID del recinto es obligatorio")
    private Long venueId;

    private String venueName;

    private Long mainArtistId;

    private String mainArtistName;

    private String statusName;

    private String flyerImage;

    private String sectionsImage;

    // Campos adicionales que faltaban
    private LocalDateTime salesStartDate;
    private LocalDateTime salesEndDate;
    private List<Long> artistIds;
    private Boolean verified;
    private Long moderatorId;
    private Long registrarId;
    
    // Nuevo campo para manejar precios de secciones
    private List<TicketPriceDTO> ticketPrices;
}