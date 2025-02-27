package com.greentrade.greentrade.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long buyerId;
    private Long productId;
    private BigDecimal amount;
    private LocalDateTime date;
    private String status;
}