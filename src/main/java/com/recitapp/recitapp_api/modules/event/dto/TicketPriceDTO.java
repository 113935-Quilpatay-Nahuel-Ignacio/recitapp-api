package com.recitapp.recitapp_api.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPriceDTO {
    private Long id;
    
    @NotNull(message = "El ID de la sección es obligatorio")
    private Long sectionId;
    
    private String sectionName; // Solo para mostrar información
    
    @NotBlank(message = "El tipo de ticket es obligatorio")
    private String ticketType;
    
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private BigDecimal price;
    
    @NotNull(message = "La cantidad disponible es obligatoria")
    @Min(value = 1, message = "La cantidad disponible debe ser mayor a cero")
    private Integer availableQuantity;
    
    private Long eventId; // Para referencia
} 