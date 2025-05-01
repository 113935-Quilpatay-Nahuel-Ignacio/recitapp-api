package com.recitapp.recitapp_api.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseHistoryDTO {
    private Long transactionId;
    private LocalDateTime purchaseDate;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String transactionStatus;
    private List<TicketPurchaseDTO> tickets;
}