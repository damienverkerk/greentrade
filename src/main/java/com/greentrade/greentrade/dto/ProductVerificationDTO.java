package com.greentrade.greentrade.dto;

import java.time.LocalDateTime;

import com.greentrade.greentrade.models.VerificationStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductVerificationDTO {
    private Long id;
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    private VerificationStatus status;
    
    private LocalDateTime verificationDate;
    
    @Size(max = 500, message = "Notes cannot be longer than 500 characters")
    private String reviewerNotes;
    
    private Long reviewerId;
    
    private LocalDateTime submissionDate;
    
    @Min(value = 0, message = "Sustainability score must be at least 0")
    @Max(value = 100, message = "Sustainability score cannot exceed 100")
    private Integer sustainabilityScore;
    
    @Size(max = 500, message = "Rejection reason cannot be longer than 500 characters")
    private String rejectionReason;
}