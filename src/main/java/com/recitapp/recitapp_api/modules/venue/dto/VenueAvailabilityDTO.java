package com.recitapp.recitapp_api.modules.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueAvailabilityDTO {
    private Long venueId;
    private String venueName;
    private Boolean isAvailable;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private List<EventConflictDTO> conflictingEvents;
}

