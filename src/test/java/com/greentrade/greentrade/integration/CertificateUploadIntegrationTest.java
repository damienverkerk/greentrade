package com.greentrade.greentrade.integration;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.test.web.servlet.MvcResult;
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
@DisplayName("Certificate Upload Integration Tests")
class CertificateUploadIntegrationTest {

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

   private CertificateCreateRequest createRequest;
   private CertificateResponse mockCertificateResponse;
   private MockMultipartFile testFile;
   private MockMultipartFile invalidFile;

   @BeforeEach
   void setUp() {
       createRequest = CertificateCreateRequest.builder()
           .name("ISO 14001")
           .issuer("Bureau Veritas")
           .issueDate(LocalDate.now())
           .expiryDate(LocalDate.now().plusYears(1))
           .description("Environmental certificate")
           .userId(2L) 
           .build();
       
       mockCertificateResponse = CertificateResponse.builder()
           .id(1L)
           .name("ISO 14001")
           .issuer("Bureau Veritas")
           .issueDate(LocalDate.now())
           .expiryDate(LocalDate.now().plusYears(1))
           .description("Environmental certificate")
           .userId(2L)
           .build();
       
       testFile = new MockMultipartFile(
           "file",
           "test-cert.pdf",
           MediaType.APPLICATION_PDF_VALUE,
           "PDF test content".getBytes()
       );
       
       invalidFile = new MockMultipartFile(
           "file",
           "test.exe",
           "application/x-msdownload",
           "Invalid content".getBytes()
       );
       
       when(fileValidationConfig.getAllowedExtensions())
            .thenReturn(Arrays.asList("pdf", "jpg", "jpeg", "png"));
       when(fileValidationConfig.getMaxFileSize())
            .thenReturn(10L * 1024 * 1024); // 10MB
    }

    @Test
    @DisplayName("Complete certificate upload - happy path")
    @WithMockUser(username = "seller@greentrade.nl", roles = "SELLER")
    void whenUploadCertificate_thenSuccess() throws Exception {
        when(certificateService.createCertificate(any(CertificateCreateRequest.class)))
            .thenReturn(mockCertificateResponse);

        when(certificateService.getCertificateById(1L))
            .thenReturn(mockCertificateResponse);
            
        when(fileStorageService.validateFileType(any(), any()))
            .thenReturn(true);
            
        when(fileStorageService.storeFile(any()))
            .thenReturn("stored-file.pdf");
        
        CertificateResponse updatedResponse = CertificateResponse.builder()
            .id(1L)
            .name("ISO 14001")
            .issuer("Bureau Veritas")
            .issueDate(LocalDate.now())
            .expiryDate(LocalDate.now().plusYears(1))
            .description("Environmental certificate")
            .filePath("stored-file.pdf")
            .userId(2L)
            .build();
            
        when(certificateService.updateCertificateFile(any(), any()))
            .thenReturn(updatedResponse);
        
        MvcResult createResult = mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        mockMvc.perform(multipart("/api/certificates/{id}/file", 1L)
                .file(testFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath").value("stored-file.pdf"));
    }
    
    @Test
    @DisplayName("Upload invalid file type - expect 400")
    @WithMockUser(roles = "SELLER")
    void whenUploadInvalidFile_thenBadRequest() throws Exception {
        when(certificateService.getCertificateById(1L))
            .thenReturn(mockCertificateResponse);
        
        mockMvc.perform(multipart("/api/certificates/{id}/file", 1L)
                .file(invalidFile))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Download non-existent file - expect 404")
    @WithMockUser(roles = "SELLER")
    void whenDownloadNonExistentFile_thenNotFound() throws Exception {
        when(certificateService.getCertificateById(999L))
            .thenReturn(null);
            
        mockMvc.perform(get("/api/certificates/999/file"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Upload without authentication - expect 403")
    void whenUploadWithoutAuth_thenForbidden() throws Exception {
        mockMvc.perform(multipart("/api/certificates/1/file")
                .file(testFile))
                .andExpect(status().isForbidden());
    }
}