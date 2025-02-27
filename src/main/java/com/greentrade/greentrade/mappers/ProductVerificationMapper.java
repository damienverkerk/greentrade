package com.greentrade.greentrade.mappers;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.ProductVerificationDTO;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.ProductVerification;
import com.greentrade.greentrade.models.User;

@Component
public class ProductVerificationMapper {
    
    public ProductVerificationDTO toDTO(ProductVerification verification) {
        if (verification == null) {
            return null;
        }
        
        ProductVerificationDTO dto = new ProductVerificationDTO();
        dto.setId(verification.getId());
        dto.setProductId(verification.getProduct().getId());
        dto.setStatus(verification.getStatus());
        dto.setVerificationDate(verification.getVerificationDate());
        dto.setReviewerNotes(verification.getReviewerNotes());
        dto.setReviewerId(verification.getReviewer() != null ? verification.getReviewer().getId() : null);
        dto.setSubmissionDate(verification.getSubmissionDate());
        dto.setSustainabilityScore(verification.getSustainabilityScore());
        dto.setRejectionReason(verification.getRejectionReason());
        
        return dto;
    }
    
    public ProductVerification toEntity(ProductVerificationDTO dto, Product product, User reviewer) {
        if (dto == null) {
            return null;
        }
        
        ProductVerification verification = new ProductVerification();
        verification.setId(dto.getId());
        verification.setProduct(product);
        verification.setStatus(dto.getStatus());
        verification.setVerificationDate(dto.getVerificationDate());
        verification.setReviewerNotes(dto.getReviewerNotes());
        verification.setReviewer(reviewer);
        verification.setSubmissionDate(dto.getSubmissionDate());
        verification.setSustainabilityScore(dto.getSustainabilityScore());
        verification.setRejectionReason(dto.getRejectionReason());
        
        return verification;
    }
}