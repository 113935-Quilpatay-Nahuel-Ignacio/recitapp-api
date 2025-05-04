package com.recitapp.recitapp_api.modules.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueSectionDTO {
    private Long id;
    private String name;
    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a cero")
    private Integer capacity;
    private String description;
    private BigDecimal basePrice;
    private Boolean active;
    private Long venueId;
}