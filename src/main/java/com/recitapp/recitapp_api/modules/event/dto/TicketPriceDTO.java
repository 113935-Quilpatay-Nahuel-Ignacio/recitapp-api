package com.recitapp.recitapp_api.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketPriceDTO {

    private Long id;

    @NotNull(message = "El ID del evento es obligatorio")
    private Long eventId;

    @NotNull(message = "El ID de la sección es obligatorio")
    private Long sectionId;

    @NotBlank(message = "El tipo de ticket es obligatorio")
    @Size(max = 50, message = "El tipo de ticket no puede exceder 50 caracteres")
    private String ticketType;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal price;

    @NotNull(message = "La cantidad disponible es obligatoria")
    @Min(value = 1, message = "La cantidad disponible debe ser mayor a cero")
    private Integer availableQuantity;
    
    // Nuevos campos para entradas promocionales y de regalo
    private Boolean isPromotional = false;
    
    private Boolean isGift = false;
    
    @Size(max = 20, message = "El tipo promocional no puede exceder 20 caracteres")
    private String promotionalType; // "2X1", "GIFT", etc.
    
    @Min(value = 1, message = "Los asientos por ticket deben ser mayor a cero")
    private Integer seatsPerTicket = 1; // Para 2x1 sería 2, para regalo 1
} 