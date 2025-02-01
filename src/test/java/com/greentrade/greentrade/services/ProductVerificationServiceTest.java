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

@ExtendWith(MockitoExtension.class)
class ProductVerificationServiceTest {

    @Mock
    private ProductVerificationRepository verificationRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductVerificationService verificationService;

    private Product testProduct;
    private User testUser;
    private ProductVerification testVerification;
    private ProductVerificationDTO testVerificationDTO;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .naam("Test Verkoper")
            .build();

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setNaam("Test Product");
        testProduct.setVerkoper(testUser);

        testVerification = new ProductVerification();
        testVerification.setId(1L);
        testVerification.setProduct(testProduct);
        testVerification.setStatus(VerificationStatus.PENDING);
        testVerification.setSubmissionDate(LocalDateTime.now());

        testVerificationDTO = new ProductVerificationDTO();
        testVerificationDTO.setId(1L);
        testVerificationDTO.setProductId(1L);
        testVerificationDTO.setStatus(VerificationStatus.APPROVED);
        testVerificationDTO.setSustainabilityScore(85);
    }

    @Test
    void whenSubmitForVerification_withValidProduct_thenSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(verificationRepository.findFirstByProductOrderBySubmissionDateDesc(testProduct))
            .thenReturn(Optional.empty());
        when(verificationRepository.save(any(ProductVerification.class))).thenReturn(testVerification);

        ProductVerificationDTO result = verificationService.submitForVerification(1L);

        assertNotNull(result);
        assertEquals(VerificationStatus.PENDING, result.getStatus());
        assertEquals(testProduct.getId(), result.getProductId());
        verify(verificationRepository).save(any(ProductVerification.class));
    }

    @Test
    void whenSubmitForVerification_withNonExistingProduct_thenThrowsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
    
    // Change expected exception type
        ProductNotFoundException thrown = assertThrows(
            ProductNotFoundException.class,  // Was ProductVerificationException.class 
            () -> verificationService.submitForVerification(999L)
        );
        assertEquals("Product niet gevonden met ID: 999", thrown.getMessage());
        verify(verificationRepository, never()).save(any(ProductVerification.class));
    }

    @Test
    void whenSubmitForVerification_withExistingVerification_thenThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(verificationRepository.findFirstByProductOrderBySubmissionDateDesc(testProduct))
            .thenReturn(Optional.of(testVerification));

        DuplicateVerificationException thrown = assertThrows(
            DuplicateVerificationException.class, 
            () -> verificationService.submitForVerification(1L)
        );
        assertEquals("Er is al een lopende verificatie voor product met ID: 1", thrown.getMessage());
        verify(verificationRepository, never()).save(any(ProductVerification.class));
    }

    @Test
    void whenReviewProduct_withValidData_thenSuccess() {
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(verificationRepository.save(any(ProductVerification.class))).thenReturn(testVerification);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductVerificationDTO result = verificationService.reviewProduct(1L, testVerificationDTO, 1L);

        assertNotNull(result);
        assertEquals(VerificationStatus.APPROVED, result.getStatus());
        assertEquals(85, result.getSustainabilityScore());
        verify(verificationRepository).save(any(ProductVerification.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void whenReviewProduct_withClosedVerification_thenThrowsException() {
        testVerification.setStatus(VerificationStatus.APPROVED);
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));

        InvalidVerificationStatusException thrown = assertThrows(
            InvalidVerificationStatusException.class,
            () -> verificationService.reviewProduct(1L, testVerificationDTO, 1L)
        );
        assertEquals("Deze verificatie kan niet meer worden beoordeeld", thrown.getMessage());
        verify(verificationRepository, never()).save(any(ProductVerification.class));
    }

    @Test
    void whenReviewProduct_withoutScore_thenThrowsException() {
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        testVerificationDTO.setSustainabilityScore(null);

        ProductVerificationException thrown = assertThrows(
            ProductVerificationException.class,
            () -> verificationService.reviewProduct(1L, testVerificationDTO, 1L)
        );
        assertEquals("Duurzaamheidsscore is verplicht bij goedkeuring", thrown.getMessage());
        verify(verificationRepository, never()).save(any(ProductVerification.class));
    }

    @Test
    void whenReviewProduct_withNonExistingVerification_thenThrowsException() {
        when(verificationRepository.findById(999L)).thenReturn(Optional.empty());
        
        VerificationNotFoundException thrown = assertThrows(
            VerificationNotFoundException.class,
            () -> verificationService.reviewProduct(999L, testVerificationDTO, 1L)
        );
        assertEquals("Verificatie niet gevonden met ID: 999", thrown.getMessage());
        verify(verificationRepository, never()).save(any(ProductVerification.class));
    }

    @Test
    void whenReviewProduct_withNonExistingReviewer_thenThrowsException() {
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        ProductVerificationException thrown = assertThrows(
            ProductVerificationException.class,
            () -> verificationService.reviewProduct(1L, testVerificationDTO, 999L)
        );
        assertEquals("Reviewer niet gevonden met ID: 999", thrown.getMessage());
        verify(verificationRepository, never()).save(any(ProductVerification.class));
    }

    @Test
    void whenGetPendingVerifications_withExistingVerifications_thenReturnsList() {
        when(verificationRepository.findByStatus(VerificationStatus.PENDING))
            .thenReturn(Arrays.asList(testVerification));

        List<ProductVerificationDTO> results = verificationService.getPendingVerifications();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(VerificationStatus.PENDING, results.get(0).getStatus());
        assertEquals(testProduct.getId(), results.get(0).getProductId());
    }

    @Test
    void whenGetPendingVerifications_withNoVerifications_thenReturnsEmptyList() {
        when(verificationRepository.findByStatus(VerificationStatus.PENDING))
            .thenReturn(Collections.emptyList());

        List<ProductVerificationDTO> results = verificationService.getPendingVerifications();

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void whenReviewProduct_withRejectionStatus_thenNoScoreRequired() {
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(verificationRepository.save(any(ProductVerification.class))).thenReturn(testVerification);

        testVerificationDTO.setStatus(VerificationStatus.REJECTED);
        testVerificationDTO.setSustainabilityScore(null);
        testVerificationDTO.setRejectionReason("Product voldoet niet aan de eisen");

        ProductVerificationDTO result = verificationService.reviewProduct(1L, testVerificationDTO, 1L);

        assertNotNull(result);
        assertEquals(VerificationStatus.REJECTED, result.getStatus());
        assertNull(result.getSustainabilityScore());
        assertNotNull(result.getRejectionReason());
        verify(verificationRepository).save(any(ProductVerification.class));
        verify(productRepository, never()).save(any(Product.class));
    }
}