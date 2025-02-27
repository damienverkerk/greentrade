package com.greentrade.greentrade.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greentrade.greentrade.dto.verification.VerificationCreateRequest;
import com.greentrade.greentrade.dto.verification.VerificationResponse;
import com.greentrade.greentrade.dto.verification.VerificationReviewRequest;
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
    public VerificationResponse submitForVerification(Long productId) {
        Product product = findProductById(productId);
        checkForExistingVerification(product);
        
        VerificationCreateRequest request = new VerificationCreateRequest(productId);
        ProductVerification verification = verificationMapper.createRequestToEntity(request, product);
        ProductVerification savedVerification = verificationRepository.save(verification);
        
        return verificationMapper.toResponse(savedVerification);
    }

    @Transactional
    public VerificationResponse reviewProduct(Long verificationId, VerificationReviewRequest request, Long reviewerId) {
        ProductVerification verification = findVerificationById(verificationId);
        validateVerificationStatus(verification);
        
        User reviewer = findReviewerById(reviewerId);
        validateReviewData(request);
        
        verificationMapper.updateFromReviewRequest(verification, request, reviewer);
        
        if (request.getStatus() == VerificationStatus.APPROVED) {
            updateProductSustainabilityScore(verification.getProduct(), request.getSustainabilityScore());
        }
        
        ProductVerification savedVerification = verificationRepository.save(verification);
        return verificationMapper.toResponse(savedVerification);
    }

    public List<VerificationResponse> getPendingVerifications() {
        try {
            return verificationRepository.findByStatus(VerificationStatus.PENDING)
                    .stream()
                    .map(verificationMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ProductVerificationException("Failed to retrieve verifications: " + e.getMessage());
        }
    }

    public List<VerificationResponse> getVerificationsByProduct(Long productId) {
        try {
            return verificationRepository.findByProductId(productId)
                    .stream()
                    .map(verificationMapper::toResponse)
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
    
    private ProductVerification findVerificationById(Long verificationId) {
        return verificationRepository.findById(verificationId)
                .orElseThrow(() -> new VerificationNotFoundException(verificationId));
    }
    
    private void validateVerificationStatus(ProductVerification verification) {
        if (verification.getStatus() != VerificationStatus.PENDING 
            && verification.getStatus() != VerificationStatus.IN_REVIEW) {
            throw new InvalidVerificationStatusException("This verification cannot be reviewed anymore");
        }
    }
    
    private User findReviewerById(Long reviewerId) {
        return userRepository.findById(reviewerId)
                .orElseThrow(() -> new UserNotFoundException(reviewerId));
    }
    
    private void validateReviewData(VerificationReviewRequest request) {
        if (request.getStatus() == VerificationStatus.APPROVED && request.getSustainabilityScore() == null) {
            throw new ProductVerificationException("Sustainability score is required for approval");
        }
        
        if (request.getStatus() == VerificationStatus.REJECTED && 
            (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty())) {
            throw new ProductVerificationException("Rejection reason is required when rejecting");
        }
    }
    
    private void updateProductSustainabilityScore(Product product, Integer sustainabilityScore) {
        product.setSustainabilityScore(sustainabilityScore);
        productRepository.save(product);
    }
}