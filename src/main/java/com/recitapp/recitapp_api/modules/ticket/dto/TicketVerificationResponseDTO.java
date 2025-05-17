package com.recitapp.recitapp_api.modules.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for ticket verification responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketVerificationResponseDTO {

    private boolean valid;
    private String status;
    private String message;

    // Ticket details (only populated if verification is successful)
    private Long ticketId;
    private String ticketCode;
    private Long eventId;
    private String eventName;
    private LocalDateTime eventDate;
    private String sectionName;
    private String attendeeName;
    private String attendeeDni;

    // Verification details
    private LocalDateTime verificationTime;
    private Long accessPointId;
    private String accessPointName;
    private Long verifierUserId;
    private String verifierName;

    // Error details if applicable
    private String errorCode;
    private String errorDetails;
}