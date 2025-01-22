package com.greentrade.greentrade.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greentrade.greentrade.dto.ProductVerificationDTO;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.ProductVerification;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.models.VerificationStatus;
import com.greentrade.greentrade.repositories.ProductRepository;
import com.greentrade.greentrade.repositories.ProductVerificationRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class ProductVerificationService {
    private final ProductVerificationRepository verificationRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductVerificationService(
            ProductVerificationRepository verificationRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.verificationRepository = verificationRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProductVerificationDTO submitForVerification(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product niet gevonden"));

        // Check of er al een lopende verificatie is
        verificationRepository.findFirstByProductOrderBySubmissionDateDesc(product)
                .ifPresent(existingVerification -> {
                    if (existingVerification.getStatus() == VerificationStatus.PENDING 
                        || existingVerification.getStatus() == VerificationStatus.IN_REVIEW) {
                        throw new RuntimeException("Er is al een lopende verificatie voor dit product");
                    }
                });

        ProductVerification verification = new ProductVerification();
        verification.setProduct(product);
        verification.setSubmissionDate(LocalDateTime.now());
        verification.setStatus(VerificationStatus.PENDING);

        return convertToDTO(verificationRepository.save(verification));
    }

    @Transactional
    public ProductVerificationDTO reviewProduct(Long verificationId, ProductVerificationDTO dto, Long reviewerId) {
        ProductVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("Verificatie niet gevonden"));

        if (verification.getStatus() != VerificationStatus.PENDING 
            && verification.getStatus() != VerificationStatus.IN_REVIEW) {
            throw new RuntimeException("Deze verificatie kan niet meer worden beoordeeld");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer niet gevonden"));

        verification.setStatus(dto.getStatus());
        verification.setReviewerNotes(dto.getReviewerNotes());
        verification.setReviewer(reviewer);
        verification.setVerificationDate(LocalDateTime.now());
        verification.setSustainabilityScore(dto.getSustainabilityScore());
        verification.setRejectionReason(dto.getRejectionReason());

        if (dto.getStatus() == VerificationStatus.APPROVED) {
            if (dto.getSustainabilityScore() == null) {
                throw new RuntimeException("Duurzaamheidsscore is verplicht bij goedkeuring");
            }
            verification.getProduct().setDuurzaamheidsScore(dto.getSustainabilityScore());
            productRepository.save(verification.getProduct());
        }

        return convertToDTO(verificationRepository.save(verification));
    }

    public List<ProductVerificationDTO> getPendingVerifications() {
        return verificationRepository.findByStatus(VerificationStatus.PENDING)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductVerificationDTO> getVerificationsByProduct(Long productId) {
        return verificationRepository.findByProductId(productId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProductVerificationDTO convertToDTO(ProductVerification verification) {
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
}