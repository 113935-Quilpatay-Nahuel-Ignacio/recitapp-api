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
public class VenueUpdateDTO {
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
}