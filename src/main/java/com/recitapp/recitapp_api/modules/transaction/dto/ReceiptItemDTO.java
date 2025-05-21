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
public class ReceiptItemDTO {
    private String itemDescription;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
