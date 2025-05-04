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
public class EventConflictDTO {
    private Long eventId;
    private String eventName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
