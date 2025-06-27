package com.recitapp.recitapp_api.modules.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueDTO {
    private Long id;
    @NotBlank(message = "El nombre del recinto es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;
    private String address;
    private String googleMapsUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer totalCapacity;
    private String description;
    private String instagramUrl;
    private String webUrl;
    private String image;
    private Boolean active;
    private LocalDateTime registrationDate;
    private LocalDateTime updatedAt;
    private List<VenueSectionDTO> sections;
    private Long registrarId;
}