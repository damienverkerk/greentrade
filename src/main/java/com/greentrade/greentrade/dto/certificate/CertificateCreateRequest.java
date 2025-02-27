package com.greentrade.greentrade.dto.certificate;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateCreateRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Issuer is required")
    private String issuer;
    
    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;
    
    private LocalDate expiryDate;
    
    private String description;
    
    private Long userId;
}