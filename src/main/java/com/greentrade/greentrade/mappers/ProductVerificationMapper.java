package com.greentrade.greentrade.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.verification.VerificationCreateRequest;
import com.greentrade.greentrade.dto.verification.VerificationResponse;
import com.greentrade.greentrade.dto.verification.VerificationReviewRequest;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.ProductVerification;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.models.VerificationStatus;

@Component
public class ProductVerificationMapper {
    
    public VerificationResponse toResponse(ProductVerification verification) {
        if (verification == null) {
            return null;
        }
        
        return VerificationResponse.builder()
                .id(verification.getId())
                .productId(verification.getProduct().getId())
                .status(verification.getStatus())
                .verificationDate(verification.getVerificationDate())
                .reviewerNotes(verification.getReviewerNotes())
                .reviewerId(verification.getReviewer() != null ? verification.getReviewer().getId() : null)
                .submissionDate(verification.getSubmissionDate())
                .sustainabilityScore(verification.getSustainabilityScore())
                .rejectionReason(verification.getRejectionReason())
                .build();
    }
    
    public ProductVerification createRequestToEntity(VerificationCreateRequest request, Product product) {
        if (request == null) {
            return null;
        }
        
        ProductVerification verification = new ProductVerification();
        verification.setProduct(product);
        verification.setStatus(VerificationStatus.PENDING);
        verification.setSubmissionDate(LocalDateTime.now());
        
        return verification;
    }
    
    public void updateFromReviewRequest(ProductVerification verification, VerificationReviewRequest request, User reviewer) {
        if (verification == null || request == null) {
            return;
        }
        
        verification.setStatus(request.getStatus());
        verification.setReviewerNotes(request.getReviewerNotes());
        verification.setReviewer(reviewer);
        verification.setVerificationDate(LocalDateTime.now());
        verification.setSustainabilityScore(request.getSustainabilityScore());
        verification.setRejectionReason(request.getRejectionReason());
    }
}