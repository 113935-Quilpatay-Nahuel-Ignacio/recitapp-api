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
public class VenueStatisticsDTO {
    private Long venueId;
    private String venueName;
    private Integer totalEvents;
    private Integer upcomingEvents;
    private Integer pastEvents;
    private Double occupancyRate;
    private LocalDateTime lastUpdateDate;
}