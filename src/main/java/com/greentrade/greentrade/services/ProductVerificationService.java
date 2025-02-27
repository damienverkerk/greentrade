package com.greentrade.greentrade.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greentrade.greentrade.dto.ProductVerificationDTO;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
import com.greentrade.greentrade.exception.verification.DuplicateVerificationException;
import com.greentrade.greentrade.exception.verification.InvalidVerificationStatusException;
import com.greentrade.greentrade.exception.verification.ProductVerificationException;
import com.greentrade.greentrade.exception.verification.VerificationNotFoundException;
import com.greentrade.greentrade.mappers.ProductVerificationMapper;
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
    private final ProductVerificationMapper verificationMapper;

    public ProductVerificationService(
            ProductVerificationRepository verificationRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            ProductVerificationMapper verificationMapper) {
        this.verificationRepository = verificationRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.verificationMapper = verificationMapper;
    }

    @Transactional
    public ProductVerificationDTO submitForVerification(Long productId) {
        Product product = findProductById(productId);
        checkForExistingVerification(product);
        
        ProductVerification verification = createPendingVerification(product);
        ProductVerification savedVerification = verificationRepository.save(verification);
        
        return verificationMapper.toDTO(savedVerification);
    }

    @Transactional
    public ProductVerificationDTO reviewProduct(Long verificationId, ProductVerificationDTO dto, Long reviewerId) {
        ProductVerification verification = findVerificationById(verificationId);
        validateVerificationStatus(verification);
        
        User reviewer = findReviewerById(reviewerId);
        validateReviewData(dto);
        
        updateVerificationWithReviewData(verification, dto, reviewer);
        
        if (dto.getStatus() == VerificationStatus.APPROVED) {
            updateProductSustainabilityScore(verification, dto);
        }
        
        return verificationMapper.toDTO(verificationRepository.save(verification));
    }

    public List<ProductVerificationDTO> getPendingVerifications() {
        try {
            return verificationRepository.findByStatus(VerificationStatus.PENDING)
                    .stream()
                    .map(verificationMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ProductVerificationException("Failed to retrieve verifications: " + e.getMessage());
        }
    }

    public List<ProductVerificationDTO> getVerificationsByProduct(Long productId) {
        try {
            return verificationRepository.findByProductId(productId)
                    .stream()
                    .map(verificationMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ProductVerificationException("Failed to retrieve product verifications: " + e.getMessage());
        }
    }
    
    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
    
    private void checkForExistingVerification(Product product) {
        verificationRepository.findFirstByProductOrderBySubmissionDateDesc(product)
                .ifPresent(existingVerification -> {
                    if (existingVerification.getStatus() == VerificationStatus.PENDING 
                        || existingVerification.getStatus() == VerificationStatus.IN_REVIEW) {
                        throw new DuplicateVerificationException(product.getId());
                    }
                });
    }
    
    private ProductVerification createPendingVerification(Product product) {
        ProductVerification verification = new ProductVerification();
        verification.setProduct(product);
        verification.setSubmissionDate(LocalDateTime.now());
        verification.setStatus(VerificationStatus.PENDING);
        return verification;
    }
    
    private ProductVerification findVerificationById(Long verificationId) {
        return verificationRepository.findById(verificationId)
                .orElseThrow(() -> new VerificationNotFoundException(verificationId));
    }
    
    private void validateVerificationStatus(ProductVerification verification) {
        if (verification.getStatus() != VerificationStatus.PENDING 
            && verification.getStatus() != VerificationStatus.IN_REVIEW) {
            throw new InvalidVerificationStatusException("Deze verificatie kan niet meer worden beoordeeld");
        }
    }
    
    private User findReviewerById(Long reviewerId) {
        return userRepository.findById(reviewerId)
                .orElseThrow(() -> new UserNotFoundException(reviewerId));
    }
    
    private void validateReviewData(ProductVerificationDTO dto) {
        if (dto.getStatus() == VerificationStatus.APPROVED && dto.getSustainabilityScore() == null) {
            throw new ProductVerificationException("Duurzaamheidsscore is verplicht bij goedkeuring");
        }
        
        if (dto.getStatus() == VerificationStatus.REJECTED && 
            (dto.getRejectionReason() == null || dto.getRejectionReason().trim().isEmpty())) {
            throw new ProductVerificationException("Reden van afwijzing is verplicht bij afkeuring");
        }
    }
    
    private void updateVerificationWithReviewData(ProductVerification verification, 
                                                 ProductVerificationDTO dto, 
                                                 User reviewer) {
        verification.setStatus(dto.getStatus());
        verification.setReviewerNotes(dto.getReviewerNotes());
        verification.setReviewer(reviewer);
        verification.setVerificationDate(LocalDateTime.now());
        verification.setSustainabilityScore(dto.getSustainabilityScore());
        verification.setRejectionReason(dto.getRejectionReason());
    }
    
    private void updateProductSustainabilityScore(ProductVerification verification, ProductVerificationDTO dto) {
        verification.getProduct().setSustainabilityScore(dto.getSustainabilityScore());
        productRepository.save(verification.getProduct());
    }
}