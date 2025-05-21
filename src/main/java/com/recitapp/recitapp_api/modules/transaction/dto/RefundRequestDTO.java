package com.recitapp.recitapp_api.modules.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDTO {
    private Long transactionId;
    private String reason;
    private Boolean fullRefund;
    private List<Long> ticketIds; // Only for partial refund
}
