package com.recitapp.recitapp_api.modules.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReportDTO {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reportType; // "USER", "PAYMENT_METHOD", "STATUS", "ALL"
    private Long userId;
    private Long paymentMethodId;
    private String statusName;
}
