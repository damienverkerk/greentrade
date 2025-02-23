package com.greentrade.greentrade.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greentrade.greentrade.dto.ProductVerificationDTO;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
import com.greentrade.greentrade.exception.verification.DuplicateVerificationException;
import com.greentrade.greentrade.exception.verification.InvalidVerificationStatusException;
import com.greentrade.greentrade.exception.verification.ProductVerificationException;
import com.greentrade.greentrade.exception.verification.VerificationNotFoundException;
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
                .orElseThrow(() -> new ProductNotFoundException(productId));  // Deze exception

        // Check of er al een lopende verificatie is
        verificationRepository.findFirstByProductOrderBySubmissionDateDesc(product)
                .ifPresent(existingVerification -> {
                    if (existingVerification.getStatus() == VerificationStatus.PENDING 
                        || existingVerification.getStatus() == VerificationStatus.IN_REVIEW) {
                        throw new DuplicateVerificationException(productId);
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
    // Controleer verificatie
    ProductVerification verification = verificationRepository.findById(verificationId)
            .orElseThrow(() -> new VerificationNotFoundException(verificationId));

    if (verification.getStatus() != VerificationStatus.PENDING 
        && verification.getStatus() != VerificationStatus.IN_REVIEW) {
        throw new InvalidVerificationStatusException("Deze verificatie kan niet meer worden beoordeeld");
    }

    // Controleer reviewer
    User reviewer = userRepository.findById(reviewerId)
            .orElseThrow(() -> new ProductVerificationException("Reviewer niet gevonden met ID: " + reviewerId));

    // Bij goedkeuring, check score
    if (dto.getStatus() == VerificationStatus.APPROVED && dto.getSustainabilityScore() == null) {
        throw new ProductVerificationException("Duurzaamheidsscore is verplicht bij goedkeuring");
    }

    // Update verificatie
    verification.setStatus(dto.getStatus());
    verification.setReviewerNotes(dto.getReviewerNotes());
    verification.setReviewer(reviewer);
    verification.setVerificationDate(LocalDateTime.now());
    verification.setSustainabilityScore(dto.getSustainabilityScore());
    verification.setRejectionReason(dto.getRejectionReason());

    if (dto.getStatus() == VerificationStatus.APPROVED) {
        verification.getProduct().setDuurzaamheidsScore(dto.getSustainabilityScore());
        productRepository.save(verification.getProduct());
    }

    return convertToDTO(verificationRepository.save(verification));
}

    public List<ProductVerificationDTO> getPendingVerifications() {
        try {
            return verificationRepository.findByStatus(VerificationStatus.PENDING)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ProductVerificationException("Ophalen van verificaties mislukt: " + e.getMessage());
        }
    }

    public List<ProductVerificationDTO> getVerificationsByProduct(Long productId) {
        try {
            return verificationRepository.findByProductId(productId)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ProductVerificationException("Ophalen van product verificaties mislukt: " + e.getMessage());
        }
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