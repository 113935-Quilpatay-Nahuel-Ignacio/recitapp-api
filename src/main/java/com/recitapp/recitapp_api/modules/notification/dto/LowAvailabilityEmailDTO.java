package com.recitapp.recitapp_api.modules.notification.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowAvailabilityEmailDTO {
    private Long userId;
    private Long eventId;
    private String recipientEmail;
    private String eventName;
    private Integer ticketsRemaining;
} 