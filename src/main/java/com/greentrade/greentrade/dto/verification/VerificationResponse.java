package com.greentrade.greentrade.dto.verification;

import java.time.LocalDateTime;

import com.greentrade.greentrade.models.VerificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {
    private Long id;
    private Long productId;
    private VerificationStatus status;
    private LocalDateTime verificationDate;
    private String reviewerNotes;
    private Long reviewerId;
    private LocalDateTime submissionDate;
    private Integer sustainabilityScore;
    private String rejectionReason;
}