package com.recitapp.recitapp_api.modules.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatisticsDTO {
    private String reportType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime generatedDate;

    // Related entity details (if applicable)
    private Long userId;
    private String userName;
    private Long paymentMethodId;
    private String paymentMethodName;
    private String statusName;

    // Summary statistics
    private int totalTransactions;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private BigDecimal maxAmount;
    private BigDecimal minAmount;

    // Detailed statistics
    private Map<String, Integer> transactionsByStatus;
    private Map<String, BigDecimal> amountByStatus;
    private Map<String, Integer> transactionsByPaymentMethod;
    private Map<String, BigDecimal> amountByPaymentMethod;

    // Time-based analytics
    private List<TimeSegmentStatisticsDTO> timeSegmentStatistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSegmentStatisticsDTO {
        private LocalDateTime segmentStart;
        private LocalDateTime segmentEnd;
        private int transactionCount;
        private BigDecimal totalAmount;
    }
}