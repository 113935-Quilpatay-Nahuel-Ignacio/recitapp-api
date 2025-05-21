package com.recitapp.recitapp_api.modules.transaction.dto;

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
public class TransactionReceiptDTO {
    private Long transactionId;
    private String receiptNumber;
    private LocalDateTime issueDate;
    private String userFullName;
    private String userDni;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private List<ReceiptItemDTO> items;
    private Boolean isRefund;
}
