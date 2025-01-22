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
    
    @NotNull(message = "Product ID is verplicht")
    private Long productId;
    
    private VerificationStatus status;
    
    private LocalDateTime verificationDate;
    
    @Size(max = 500, message = "Opmerkingen mogen niet langer zijn dan 500 karakters")
    private String reviewerNotes;
    
    private Long reviewerId;
    
    private LocalDateTime submissionDate;
    
    @Min(value = 0, message = "Duurzaamheidsscore moet minimaal 0 zijn")
    @Max(value = 100, message = "Duurzaamheidsscore mag maximaal 100 zijn")
    private Integer sustainabilityScore;
    
    @Size(max = 500, message = "Reden voor afwijzing mag niet langer zijn dan 500 karakters")
    private String rejectionReason;
}