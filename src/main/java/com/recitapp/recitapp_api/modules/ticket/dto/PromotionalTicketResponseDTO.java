package com.recitapp.recitapp_api.modules.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for the response after creating promotional tickets
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionalTicketResponseDTO {
    private Long eventId;
    private String eventName;
    private String promotionName;
    private String promotionDescription;
    private LocalDateTime creationDate;
    private Long adminUserId;
    private String adminUserName;
    private Integer ticketCount;
    private List<TicketDTO> tickets;
}