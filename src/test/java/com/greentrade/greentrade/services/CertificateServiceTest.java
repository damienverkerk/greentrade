package com.greentrade.greentrade.services;

import java.time.LocalDate;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greentrade.greentrade.dto.CertificateDTO;
import com.greentrade.greentrade.models.Certificate;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.CertificateRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private CertificateService certificateService;

    private Certificate testCertificate;
    private User testUser;
    private CertificateDTO testCertificateDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        testCertificate = new Certificate();
        testCertificate.setId(1L);
        testCertificate.setName("ISO 14001");
        testCertificate.setIssuer("Bureau Veritas");
        testCertificate.setIssueDate(LocalDate.now());
        testCertificate.setExpiryDate(LocalDate.now().plusYears(1));
        testCertificate.setDescription("Environmental Management System");
        testCertificate.setFilePath("iso14001.pdf");
        testCertificate.setUser(testUser);

        testCertificateDTO = new CertificateDTO(
                1L,
                "ISO 14001",
                "Bureau Veritas",
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                "Environmental Management System",
                "iso14001.pdf",
                1L
        );
    }

    @Test
    void getAllCertificates_ReturnsAllCertificates() {
        // Arrange
        when(certificateRepository.findAll()).thenReturn(Arrays.asList(testCertificate));

        // Act
        List<CertificateDTO> result = certificateService.getAllCertificates();

        // Assert
        assertEquals(1, result.size());
        assertEquals("ISO 14001", result.get(0).getName());
    }

    @Test
    void getCertificateById_ExistingCertificate_ReturnsCertificate() {
        // Arrange
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(testCertificate));

        // Act
        CertificateDTO result = certificateService.getCertificateById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("ISO 14001", result.getName());
        assertEquals("Bureau Veritas", result.getIssuer());
    }

    @Test
    void getCertificateById_NonExistingCertificate_ReturnsNull() {
        // Arrange
        when(certificateRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        CertificateDTO result = certificateService.getCertificateById(99L);

        // Assert
        assertEquals(null, result);
    }

    @Test
    void getCertificatesForUser_ExistingUser_ReturnsCertificates() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(certificateRepository.findByUser(testUser)).thenReturn(Arrays.asList(testCertificate));

        // Act
        List<CertificateDTO> result = certificateService.getCertificatesForUser(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("ISO 14001", result.get(0).getName());
    }

    @Test
    void getCertificatesForUser_NonExistingUser_ThrowsException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown = assertThrows(
            RuntimeException.class,
            () -> certificateService.getCertificatesForUser(99L)
        );
        assertEquals("User not found with id: 99", thrown.getMessage());
    }

    @Test
    void getExpiredCertificates_ReturnsCertificates() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(certificateRepository.findByExpiryDateBefore(date)).thenReturn(Arrays.asList(testCertificate));

        // Act
        List<CertificateDTO> result = certificateService.getExpiredCertificates(date);

        // Assert
        assertEquals(1, result.size());
        assertEquals("ISO 14001", result.get(0).getName());
    }

    @Test
    void createCertificate_SavesAndReturnsCertificate() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(testCertificate);

        // Act
        CertificateDTO result = certificateService.createCertificate(testCertificateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("ISO 14001", result.getName());
        assertEquals("Bureau Veritas", result.getIssuer());
        verify(certificateRepository, times(1)).save(any(Certificate.class));
    }

    @Test
    void updateCertificate_WithExistingCertificate_UpdatesAndReturns() {
        // Arrange
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(testCertificate));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(testCertificate);

        testCertificateDTO.setName("Updated ISO 14001");

        // Act
        CertificateDTO result = certificateService.updateCertificate(1L, testCertificateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated ISO 14001", result.getName());
        verify(certificateRepository, times(1)).save(any(Certificate.class));
    }

    @Test
    void updateCertificateFile_UpdatesFilePathAndReturns() {
        // Arrange
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(testCertificate));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(testCertificate);

        testCertificate.setFilePath("old-file.pdf");

        // Act
        CertificateDTO result = certificateService.updateCertificateFile(1L, "new-file.pdf");

        // Assert
        assertNotNull(result);
        assertEquals("new-file.pdf", result.getFilePath());
        verify(certificateRepository, times(1)).save(any(Certificate.class));
    }
}