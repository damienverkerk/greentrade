package com.greentrade.greentrade.dto.verification;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCreateRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;
}