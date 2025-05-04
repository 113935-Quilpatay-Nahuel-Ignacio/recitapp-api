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
public class VenueFollowerDTO {
    private Long venueId;
    private String venueName;
    private String venueAddress;
    private String venueImage;
    private LocalDateTime followDate;
}