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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greentrade.greentrade.dto.certificate.CertificateCreateRequest;
import com.greentrade.greentrade.dto.certificate.CertificateResponse;
import com.greentrade.greentrade.dto.certificate.CertificateUpdateRequest;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
import com.greentrade.greentrade.mappers.CertificateMapper;
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
    
    @Mock
    private CertificateMapper certificateMapper;

    @InjectMocks
    private CertificateService certificateService;

    private Certificate testCertificate;
    private User testUser;
    private CertificateCreateRequest createRequest;
    private CertificateUpdateRequest updateRequest;
    private CertificateResponse certificateResponse;

    @BeforeEach
    void setUp() {
        // Arrange - Create test user
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        // Arrange - Create test certificate
        testCertificate = new Certificate();
        testCertificate.setId(1L);
        testCertificate.setName("ISO 14001");
        testCertificate.setIssuer("Bureau Veritas");
        testCertificate.setIssueDate(LocalDate.now());
        testCertificate.setExpiryDate(LocalDate.now().plusYears(1));
        testCertificate.setDescription("Environmental Management System");
        testCertificate.setFilePath("iso14001.pdf");
        testCertificate.setUser(testUser);
        
        // Arrange - Create test requests
        createRequest = CertificateCreateRequest.builder()
                .name("ISO 14001")
                .issuer("Bureau Veritas")
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(1))
                .description("Environmental Management System")
                .userId(1L)
                .build();
                
        updateRequest = CertificateUpdateRequest.builder()
                .name("Updated ISO 14001")
                .issuer("Bureau Veritas")
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(1))
                .description("Updated Environmental Management System")
                .userId(1L)
                .build();
        
        // Arrange - Create test response
        certificateResponse = CertificateResponse.builder()
                .id(1L)
                .name("ISO 14001")
                .issuer("Bureau Veritas")
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(1))
                .description("Environmental Management System")
                .filePath("iso14001.pdf")
                .userId(1L)
                .build();
    }

    @Test
    void getAllCertificates_ReturnsAllCertificates() {
        // Arrange
        when(certificateRepository.findAll()).thenReturn(Arrays.asList(testCertificate));
        when(certificateMapper.toResponse(testCertificate)).thenReturn(certificateResponse);

        // Act
        List<CertificateResponse> result = certificateService.getAllCertificates();

        // Assert
        assertEquals(1, result.size());
        assertEquals("ISO 14001", result.get(0).getName());
        verify(certificateRepository, times(1)).findAll();
    }

    @Test
    void getCertificateById_ExistingCertificate_ReturnsCertificate() {
        // Arrange
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(testCertificate));
        when(certificateMapper.toResponse(testCertificate)).thenReturn(certificateResponse);

        // Act
        CertificateResponse result = certificateService.getCertificateById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("ISO 14001", result.getName());
        assertEquals("Bureau Veritas", result.getIssuer());
        verify(certificateRepository, times(1)).findById(1L);
    }

    @Test
    void getCertificateById_NonExistingCertificate_ReturnsNull() {
        // Arrange
        when(certificateRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        CertificateResponse result = certificateService.getCertificateById(99L);

        // Assert
        assertEquals(null, result);
        verify(certificateRepository, times(1)).findById(99L);
    }

    @Test
    void getCertificatesForUser_ExistingUser_ReturnsCertificates() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(certificateRepository.findByUser(testUser)).thenReturn(Arrays.asList(testCertificate));
        when(certificateMapper.toResponse(testCertificate)).thenReturn(certificateResponse);

        // Act
        List<CertificateResponse> result = certificateService.getCertificatesForUser(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("ISO 14001", result.get(0).getName());
        verify(userRepository, times(1)).findById(1L);
        verify(certificateRepository, times(1)).findByUser(testUser);
    }

    @Test
    void getCertificatesForUser_NonExistingUser_ThrowsException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
            UserNotFoundException.class,
            () -> certificateService.getCertificatesForUser(99L)
        );
        assertEquals("User not found with ID: 99", thrown.getMessage());
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void getExpiredCertificates_ReturnsCertificates() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(certificateRepository.findByExpiryDateBefore(date)).thenReturn(Arrays.asList(testCertificate));
        when(certificateMapper.toResponse(testCertificate)).thenReturn(certificateResponse);

        // Act
        List<CertificateResponse> result = certificateService.getExpiredCertificates(date);

        // Assert
        assertEquals(1, result.size());
        assertEquals("ISO 14001", result.get(0).getName());
        verify(certificateRepository, times(1)).findByExpiryDateBefore(date);
    }

    @Test
    void createCertificate_SavesAndReturnsCertificate() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(certificateMapper.createRequestToEntity(createRequest, testUser)).thenReturn(testCertificate);
        when(certificateRepository.save(testCertificate)).thenReturn(testCertificate);
        when(certificateMapper.toResponse(testCertificate)).thenReturn(certificateResponse);

        // Act
        CertificateResponse result = certificateService.createCertificate(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("ISO 14001", result.getName());
        assertEquals("Bureau Veritas", result.getIssuer());
        verify(userRepository, times(1)).findById(1L);
        verify(certificateMapper, times(1)).createRequestToEntity(createRequest, testUser);
        verify(certificateRepository, times(1)).save(testCertificate);
    }

    @Test
    void updateCertificate_WithExistingCertificate_UpdatesAndReturns() {
        // Arrange
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(testCertificate));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(certificateRepository.save(testCertificate)).thenReturn(testCertificate);
        
        CertificateResponse updatedResponse = CertificateResponse.builder()
                .id(1L)
                .name("Updated ISO 14001")
                .issuer("Bureau Veritas")
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(1))
                .description("Updated Environmental Management System")
                .filePath("iso14001.pdf")
                .userId(1L)
                .build();
                
        when(certificateMapper.toResponse(testCertificate)).thenReturn(updatedResponse);

        // Act
        CertificateResponse result = certificateService.updateCertificate(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated ISO 14001", result.getName());
        verify(certificateRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(certificateMapper, times(1)).updateEntityFromRequest(testCertificate, updateRequest, testUser);
        verify(certificateRepository, times(1)).save(testCertificate);
    }

    @Test
    void updateCertificateFile_UpdatesFilePathAndReturns() {
        // Arrange
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(testCertificate));
        
        testCertificate.setFilePath("old-file.pdf");
        when(certificateRepository.save(testCertificate)).thenReturn(testCertificate);
        
        CertificateResponse updatedResponse = CertificateResponse.builder()
                .id(1L)
                .name("ISO 14001")
                .issuer("Bureau Veritas")
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(1))
                .description("Environmental Management System")
                .filePath("new-file.pdf")
                .userId(1L)
                .build();
                
        when(certificateMapper.toResponse(testCertificate)).thenReturn(updatedResponse);

        // Act
        CertificateResponse result = certificateService.updateCertificateFile(1L, "new-file.pdf");

        // Assert
        assertNotNull(result);
        assertEquals("new-file.pdf", result.getFilePath());
        verify(certificateRepository, times(1)).findById(1L);
        verify(certificateRepository, times(1)).save(testCertificate);
    }
    
    @Test
    void deleteCertificate_WithExistingCertificate_DeletesSuccessfully() {
        // Arrange
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(testCertificate));
        testCertificate.setFilePath("file-to-delete.pdf");
        
        // Act
        certificateService.deleteCertificate(1L);
        
        // Assert
        verify(certificateRepository, times(1)).findById(1L);
        verify(fileStorageService, times(1)).deleteFile("file-to-delete.pdf");
        verify(certificateRepository, times(1)).deleteById(1L);
    }
    
}