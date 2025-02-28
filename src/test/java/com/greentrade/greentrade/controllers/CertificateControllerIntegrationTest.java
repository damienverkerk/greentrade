package com.greentrade.greentrade.controllers;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.config.FileValidationConfig;
import com.greentrade.greentrade.dto.certificate.CertificateCreateRequest;
import com.greentrade.greentrade.dto.certificate.CertificateResponse;
import com.greentrade.greentrade.services.CertificateService;
import com.greentrade.greentrade.services.FileStorageService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
class CertificateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CertificateService certificateService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private FileValidationConfig fileValidationConfig;

    private CertificateResponse testCertificate;
    private CertificateCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        
        testCertificate = CertificateResponse.builder()
            .id(1L)
            .name("ISO14001")
            .issuer("Bureau Veritas")
            .issueDate(LocalDate.now())
            .expiryDate(LocalDate.now().plusYears(1))
            .description("Environmental certificate")
            .filePath("cert1.pdf")
            .userId(1L)
            .build();
            
        
        createRequest = CertificateCreateRequest.builder()
            .name("ISO14001")
            .issuer("Bureau Veritas")
            .issueDate(LocalDate.now())
            .expiryDate(LocalDate.now().plusYears(1))
            .description("Environmental certificate")
            .userId(1L)
            .build();
    }

    @Test
    @WithMockUser
    void whenUploadCertificateFile_thenSuccess() throws Exception {
        
        when(fileValidationConfig.getAllowedExtensions())
            .thenReturn(Arrays.asList("pdf", "jpg", "jpeg", "png"));
        when(fileValidationConfig.getMaxFileSize())
            .thenReturn(10L * 1024 * 1024); // 10MB
        
        when(certificateService.getCertificateById(1L))
            .thenReturn(testCertificate);
            
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "test content".getBytes()
        );

        
        when(fileStorageService.validateFileType(any(), eq(new String[]{"pdf", "jpg", "jpeg", "png"})))
            .thenReturn(true);
        when(fileStorageService.storeFile(file)).thenReturn("stored_test.pdf");
        
        CertificateResponse updatedCert = CertificateResponse.builder()
            .id(1L)
            .name("ISO14001")
            .issuer("Bureau Veritas")
            .issueDate(LocalDate.now())
            .expiryDate(LocalDate.now().plusYears(1))
            .description("Environmental certificate")
            .filePath("stored_test.pdf")
            .userId(1L)
            .build();
            
        when(certificateService.updateCertificateFile(eq(1L), eq("stored_test.pdf")))
            .thenReturn(updatedCert);

        
        mockMvc.perform(multipart("/api/certificates/{id}/file", 1L)
                .file(file)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath").value("stored_test.pdf"));
    }

    @Test
    @WithMockUser
    void whenGetAllCertificates_thenSuccess() throws Exception {
        
        when(certificateService.getAllCertificates())
            .thenReturn(Arrays.asList(testCertificate));

        
        mockMvc.perform(get("/api/certificates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ISO14001"));
    }

    @Test
    @WithMockUser
    void whenCreateCertificate_thenSuccess() throws Exception {
        
        when(certificateService.createCertificate(any(CertificateCreateRequest.class)))
            .thenReturn(testCertificate);

       
        mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ISO14001"));
    }

    @Test
    @WithMockUser
    void whenGetExpiredCertificates_thenSuccess() throws Exception {
        
        when(certificateService.getExpiredCertificates(any()))
            .thenReturn(Arrays.asList(testCertificate));

        
        mockMvc.perform(get("/api/certificates/expired")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ISO14001"));
    }

    @Test
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        
        mockMvc.perform(get("/api/certificates"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser
    void whenGetCertificateById_thenSuccess() throws Exception {
        
        when(certificateService.getCertificateById(1L))
            .thenReturn(testCertificate);

        
        mockMvc.perform(get("/api/certificates/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ISO14001"));
    }
    
    @Test
    @WithMockUser
    void whenGetCertificateById_notFound_thenReturn404() throws Exception {
        
        when(certificateService.getCertificateById(99L))
            .thenReturn(null);

        
        mockMvc.perform(get("/api/certificates/{id}", 99L))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser
    void whenGetCertificatesForUser_thenSuccess() throws Exception {
        
        when(certificateService.getCertificatesForUser(1L))
            .thenReturn(Arrays.asList(testCertificate));

        
        mockMvc.perform(get("/api/certificates/user/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ISO14001"));
    }
}