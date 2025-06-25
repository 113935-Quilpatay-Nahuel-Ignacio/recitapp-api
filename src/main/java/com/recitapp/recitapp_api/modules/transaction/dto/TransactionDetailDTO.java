package com.recitapp.recitapp_api.modules.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailDTO {
    private Long ticketId;
    private String ticketCode;
    private String eventName;
    private BigDecimal unitPrice;
    private String ticketStatus; // Status of the ticket (VENDIDA, CANCELADA, etc.)
    private Boolean isRefunded; // Whether this ticket has been refunded
}
