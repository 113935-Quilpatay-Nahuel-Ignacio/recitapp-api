package com.recitapp.recitapp_api.modules.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long paymentMethodId;
    private String paymentMethodName;
    private BigDecimal totalAmount;
    private String statusName;
    private String externalReference;
    private LocalDateTime transactionDate;
    private List<TransactionDetailDTO> details;
    private String description;
    private Boolean isRefund;
    private Long originalTransactionId;
}

