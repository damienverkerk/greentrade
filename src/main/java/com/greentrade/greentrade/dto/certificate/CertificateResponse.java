package com.greentrade.greentrade.dto.certificate;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {
    private Long id;
    private String name;
    private String issuer;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String description;
    private String filePath;
    private Long userId;
}