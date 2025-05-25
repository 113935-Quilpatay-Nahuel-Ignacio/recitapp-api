package com.recitapp.recitapp_api.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateDTO {
    private Long userId;
    private String typeName;
    private String channelName;
    private String content;
    private Long relatedEventId;
    private Long relatedArtistId;
    private Long relatedVenueId;
}