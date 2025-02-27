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
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.config.FileValidationConfig;
import com.greentrade.greentrade.dto.CertificateDTO;
import com.greentrade.greentrade.services.CertificateService;
import com.greentrade.greentrade.services.FileStorageService;

@SpringBootTest
@AutoConfigureMockMvc
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

    private CertificateDTO testCertificate;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testCertificate = new CertificateDTO(1L, "ISO14001", "Bureau Veritas", 
            LocalDate.now(), LocalDate.now().plusYears(1), 
            "Environmental certificate", "cert1.pdf", 1L);
    }

    @Test
    @WithMockUser
    void whenUploadCertificateFile_thenSuccess() throws Exception {
        // Mock config
        when(fileValidationConfig.getAllowedExtensions())
            .thenReturn(Arrays.asList("pdf", "jpg", "jpeg", "png"));
        when(fileValidationConfig.getMaxFileSize())
            .thenReturn(10L * 1024 * 1024); // 10MB
        
        when(certificateService.getCertificateById(1L))
            .thenReturn(testCertificate);
        // Create test file
        MockMultipartFile file = new MockMultipartFile(
            "file",          // parameter name in controller
            "test.pdf",      // original filename
            MediaType.APPLICATION_PDF_VALUE,
            "test content".getBytes()
        );

        // Mock validations and services
        when(fileStorageService.validateFileType(any(), eq(new String[]{"pdf", "jpg", "jpeg", "png"})))
            .thenReturn(true);
        when(fileStorageService.storeFile(file)).thenReturn("stored_test.pdf");
        
        CertificateDTO updatedCert = new CertificateDTO(
            1L, "ISO14001", "Bureau Veritas",
            LocalDate.now(), LocalDate.now().plusYears(1),
            "Environmental certificate", "stored_test.pdf", 1L
        );
        when(certificateService.updateCertificateFile(eq(1L), eq("stored_test.pdf")))
            .thenReturn(updatedCert);

        // Run test
        mockMvc.perform(multipart("/api/certificates/{id}/file", 1L)
                .file(file)
                .characterEncoding("UTF-8"))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
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
        when(certificateService.createCertificate(any(CertificateDTO.class)))
            .thenReturn(testCertificate);

        mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCertificate)))
                .andExpect(status().isOk());
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
}