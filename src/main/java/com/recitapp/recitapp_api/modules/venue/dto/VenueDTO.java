package com.recitapp.recitapp_api.modules.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueDTO {
    private Long id;
    private String name;
    private String address;
    private String googleMapsUrl;
    private Integer totalCapacity;
    private String description;
    private String instagramUrl;
    private String webUrl;
    private String image;
    private LocalDateTime registrationDate;
}