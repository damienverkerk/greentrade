package com.greentrade.greentrade.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greentrade.greentrade.dto.verification.VerificationCreateRequest;
import com.greentrade.greentrade.dto.verification.VerificationResponse;
import com.greentrade.greentrade.dto.verification.VerificationReviewRequest;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
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

@ExtendWith(MockitoExtension.class)
class ProductVerificationServiceTest {

    @Mock
    private ProductVerificationRepository verificationRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProductVerificationMapper verificationMapper;

    @InjectMocks
    private ProductVerificationService verificationService;

    private Product testProduct;
    private User testUser;
    private ProductVerification testVerification;
    private VerificationResponse testVerificationResponse;
    private VerificationCreateRequest createRequest;
    private VerificationReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        // Arrange - Create test user
        testUser = User.builder()
            .id(1L)
            .name("Test Reviewer")
            .build();

        // Arrange - Create test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setSeller(testUser);

        // Arrange - Create test verification
        testVerification = new ProductVerification();
        testVerification.setId(1L);
        testVerification.setProduct(testProduct);
        testVerification.setStatus(VerificationStatus.PENDING);
        testVerification.setSubmissionDate(LocalDateTime.now());
        
        // Arrange - Create test verification response
        testVerificationResponse = VerificationResponse.builder()
            .id(1L)
            .productId(1L)
            .status(VerificationStatus.PENDING)
            .submissionDate(LocalDateTime.now())
            .build();
            
        // Arrange - Create request objects
        createRequest = VerificationCreateRequest.builder()
            .productId(1L)
            .build();
            
        reviewRequest = VerificationReviewRequest.builder()
            .status(VerificationStatus.APPROVED)
            .reviewerNotes("Product meets sustainability criteria")
            .sustainabilityScore(85)
            .build();
    }

    @Test
    void submitForVerification_WithValidProduct_CreatesVerification() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(verificationRepository.findFirstByProductOrderBySubmissionDateDesc(testProduct))
            .thenReturn(Optional.empty());
        when(verificationMapper.createRequestToEntity(any(VerificationCreateRequest.class), any(Product.class)))
            .thenReturn(testVerification);
        when(verificationRepository.save(any(ProductVerification.class))).thenReturn(testVerification);
        when(verificationMapper.toResponse(testVerification)).thenReturn(testVerificationResponse);

        // Act
        VerificationResponse result = verificationService.submitForVerification(1L);

        // Assert
        assertNotNull(result);
        assertEquals(VerificationStatus.PENDING, result.getStatus());
        assertEquals(testProduct.getId(), result.getProductId());
        verify(productRepository).findById(1L);
        verify(verificationRepository).findFirstByProductOrderBySubmissionDateDesc(testProduct);
        verify(verificationRepository).save(any(ProductVerification.class));
    }

    @Test
    void submitForVerification_WithNonExistingProduct_ThrowsException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
    
        // Act & Assert
        ProductNotFoundException thrown = assertThrows(
            ProductNotFoundException.class,  
            () -> verificationService.submitForVerification(999L)
        );
        assertEquals("Product niet gevonden met ID: 999", thrown.getMessage());
        verify(verificationRepository, never()).save(any(ProductVerification.class));
    }

    @Test
    void submitForVerification_WithExistingPendingVerification_ThrowsException() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(verificationRepository.findFirstByProductOrderBySubmissionDateDesc(testProduct))
            .thenReturn(Optional.of(testVerification));

        // Act & Assert
        DuplicateVerificationException thrown = assertThrows(
            DuplicateVerificationException.class, 
            () -> verificationService.submitForVerification(1L)
        );
        assertEquals("Er is al een lopende verificatie voor product met ID: 1", thrown.getMessage());
        verify(verificationRepository, never()).save(any(ProductVerification.class));
    }

    @Test
    void reviewProduct_WithValidData_UpdatesVerification() {
        // Arrange
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        VerificationResponse approvedResponse = VerificationResponse.builder()
            .id(1L)
            .productId(1L)
            .status(VerificationStatus.APPROVED)
            .reviewerNotes("Product meets sustainability criteria")
            .reviewerId(1L)
            .sustainabilityScore(85)
            .build();
            
        when(verificationRepository.save(any(ProductVerification.class))).thenReturn(testVerification);
        when(verificationMapper.toResponse(testVerification)).thenReturn(approvedResponse);

        // Act
        VerificationResponse result = verificationService.reviewProduct(1L, reviewRequest, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(VerificationStatus.APPROVED, result.getStatus());
        assertEquals(85, result.getSustainabilityScore());
        verify(verificationRepository).save(any(ProductVerification.class));
    }

    @Test
    void reviewProduct_WithClosedVerification_ThrowsException() {
        // Arrange
        testVerification.setStatus(VerificationStatus.APPROVED);
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));

        // Act & Assert
        InvalidVerificationStatusException thrown = assertThrows(
            InvalidVerificationStatusException.class,
            () -> verificationService.reviewProduct(1L, reviewRequest, 1L)
        );
        assertEquals("Deze verificatie kan niet meer worden beoordeeld", thrown.getMessage());
        verify(verificationRepository, never()).save(any(ProductVerification.class));
    }

    @Test
    void reviewProduct_WithoutScore_ThrowsException() {
        // Arrange
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        reviewRequest.setSustainabilityScore(null);

        // Act & Assert
        ProductVerificationException thrown = assertThrows(
            ProductVerificationException.class,
            () -> verificationService.reviewProduct(1L, reviewRequest, 1L)
        );
        assertEquals("Duurzaamheidsscore is verplicht bij goedkeuring", thrown.getMessage());
        verify(verificationRepository, never()).save(any(ProductVerification.class));
    }

    @Test
    void reviewProduct_WithNonExistingVerification_ThrowsException() {
        // Arrange
        when(verificationRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        VerificationNotFoundException thrown = assertThrows(
            VerificationNotFoundException.class,
            () -> verificationService.reviewProduct(999L, reviewRequest, 1L)
        );
        assertEquals("Verificatie niet gevonden met ID: 999", thrown.getMessage());
        verify(verificationRepository, never()).save(any(ProductVerification.class));
    }

    @Test
    void getPendingVerifications_WithExistingVerifications_ReturnsList() {
        // Arrange
        when(verificationRepository.findByStatus(VerificationStatus.PENDING))
            .thenReturn(Arrays.asList(testVerification));
        when(verificationMapper.toResponse(testVerification)).thenReturn(testVerificationResponse);

        // Act
        List<VerificationResponse> results = verificationService.getPendingVerifications();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(VerificationStatus.PENDING, results.get(0).getStatus());
        assertEquals(testProduct.getId(), results.get(0).getProductId());
        verify(verificationRepository).findByStatus(VerificationStatus.PENDING);
    }

    @Test
    void getPendingVerifications_WithNoVerifications_ReturnsEmptyList() {
        // Arrange
        when(verificationRepository.findByStatus(VerificationStatus.PENDING))
            .thenReturn(Collections.emptyList());

        // Act
        List<VerificationResponse> results = verificationService.getPendingVerifications();

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(verificationRepository).findByStatus(VerificationStatus.PENDING);
    }

    @Test
    void reviewProduct_WithRejectionStatus_NoScoreRequired() {
        // Arrange
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        reviewRequest.setStatus(VerificationStatus.REJECTED);
        reviewRequest.setSustainabilityScore(null);
        reviewRequest.setRejectionReason("Product voldoet niet aan de eisen");
        
        VerificationResponse rejectedResponse = VerificationResponse.builder()
            .id(1L)
            .productId(1L)
            .status(VerificationStatus.REJECTED)
            .reviewerNotes("Product does not meet criteria")
            .reviewerId(1L)
            .rejectionReason("Product voldoet niet aan de eisen")
            .build();

        when(verificationRepository.save(any(ProductVerification.class))).thenReturn(testVerification);
        when(verificationMapper.toResponse(testVerification)).thenReturn(rejectedResponse);

        // Act
        VerificationResponse result = verificationService.reviewProduct(1L, reviewRequest, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(VerificationStatus.REJECTED, result.getStatus());
        assertNull(result.getSustainabilityScore());
        assertNotNull(result.getRejectionReason());
        verify(verificationRepository).save(any(ProductVerification.class));
    }
    
    @Test
    void getVerificationsByProduct_ReturnsVerificationsList() {
        // Arrange
        when(verificationRepository.findByProductId(1L))
            .thenReturn(Arrays.asList(testVerification));
        when(verificationMapper.toResponse(testVerification)).thenReturn(testVerificationResponse);

        // Act
        List<VerificationResponse> results = verificationService.getVerificationsByProduct(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getProductId());
        verify(verificationRepository).findByProductId(1L);
    }
}