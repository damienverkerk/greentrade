package com.greentrade.greentrade.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greentrade.greentrade.dto.ProductVerificationDTO;
import com.greentrade.greentrade.exception.verification.DuplicateVerificationException;
import com.greentrade.greentrade.exception.verification.InvalidVerificationStatusException;
import com.greentrade.greentrade.exception.verification.ProductVerificationException;
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
    void indienProductVoorVerificatieGelukt() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(verificationRepository.findFirstByProductOrderBySubmissionDateDesc(testProduct))
            .thenReturn(Optional.empty());
        when(verificationRepository.save(any(ProductVerification.class))).thenReturn(testVerification);

        ProductVerificationDTO result = verificationService.submitForVerification(1L);

        assertNotNull(result);
        assertEquals(VerificationStatus.PENDING, result.getStatus());
        verify(verificationRepository).save(any(ProductVerification.class));
    }

    @Test
    void indienProductMetBestaandeVerificatieGeeftFout() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(verificationRepository.findFirstByProductOrderBySubmissionDateDesc(testProduct))
            .thenReturn(Optional.of(testVerification));

        assertThrows(DuplicateVerificationException.class, () -> 
            verificationService.submitForVerification(1L)
        );
    }

    @Test
    void beoordeelProductVerificatieGelukt() {
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(verificationRepository.save(any(ProductVerification.class))).thenReturn(testVerification);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductVerificationDTO result = verificationService.reviewProduct(1L, testVerificationDTO, 1L);

        assertNotNull(result);
        assertEquals(VerificationStatus.APPROVED, result.getStatus());
        assertEquals(85, result.getSustainabilityScore());
    }

    @Test
    void beoordeelAfgeslotenVerificatieGeeftFout() {
        testVerification.setStatus(VerificationStatus.APPROVED);
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));

        assertThrows(InvalidVerificationStatusException.class, () ->
            verificationService.reviewProduct(1L, testVerificationDTO, 1L)
        );
    }

    @Test
    void beoordeelZonderScoreGeeftFout() {
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(testVerification));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        testVerificationDTO.setSustainabilityScore(null);

        assertThrows(ProductVerificationException.class, () ->
            verificationService.reviewProduct(1L, testVerificationDTO, 1L)
        );
    }

    @Test
    void haalPendingVerificatiesOp() {
        when(verificationRepository.findByStatus(VerificationStatus.PENDING))
            .thenReturn(Arrays.asList(testVerification));

        List<ProductVerificationDTO> results = verificationService.getPendingVerifications();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(VerificationStatus.PENDING, results.get(0).getStatus());
    }
}