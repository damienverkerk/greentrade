package com.greentrade.greentrade.dto.verification;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.greentrade.greentrade.models.VerificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationReviewRequest {
    @NotNull(message = "Status is required")
    private VerificationStatus status;
    
    @Size(max = 500, message = "Notes cannot be longer than 500 characters")
    private String reviewerNotes;
    
    @Min(value = 0, message = "Sustainability score must be at least 0")
    @Max(value = 100, message = "Sustainability score cannot exceed 100")
    private Integer sustainabilityScore;
    
    @Size(max = 500, message = "Rejection reason cannot be longer than 500 characters")
    private String rejectionReason;
}