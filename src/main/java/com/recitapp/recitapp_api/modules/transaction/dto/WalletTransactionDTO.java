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
public class WalletTransactionDTO {
    private Long userId;
    private BigDecimal amount;
    private String operation; // "ADD" or "SUBTRACT"
    private String description;
}
