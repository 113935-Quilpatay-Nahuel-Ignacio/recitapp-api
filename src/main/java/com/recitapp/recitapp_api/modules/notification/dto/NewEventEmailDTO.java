package com.recitapp.recitapp_api.modules.notification.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventEmailDTO {
    private Long userId;
    private Long eventId;
    private String recipientEmail;
    private String eventName;
    private String artistName;
    private String eventDate;
    private String venueName;
} 