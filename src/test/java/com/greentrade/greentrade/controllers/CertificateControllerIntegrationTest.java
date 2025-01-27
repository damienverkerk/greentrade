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

    private CertificateDTO testCertificaat;

    @BeforeEach
    void setUp() {
        testCertificaat = new CertificateDTO(1L, "ISO14001", "Bureau Veritas", 
            LocalDate.now(), LocalDate.now().plusYears(1), 
            "Milieu certificaat", "cert1.pdf", 1L);
    }

    @Test
    @WithMockUser
    void whenUploadCertificate_thenSuccess() throws Exception {
        // Mock config
        when(fileValidationConfig.getAllowedExtensions())
            .thenReturn(Arrays.asList("pdf", "jpg", "jpeg", "png"));
        when(fileValidationConfig.getMaxFileSize())
            .thenReturn(10L * 1024 * 1024); // 10MB

        // Maak testbestand
        MockMultipartFile file = new MockMultipartFile(
            "bestand",          // naam parameter in controller
            "test.pdf",         // originele bestandsnaam
            MediaType.APPLICATION_PDF_VALUE,
            "test content".getBytes()
        );

        // Mock validaties en services
        when(fileStorageService.validateFileType(any(), eq(new String[]{"pdf", "jpg", "jpeg", "png"})))
            .thenReturn(true);
        when(fileStorageService.storeFile(file)).thenReturn("stored_test.pdf");
        
        CertificateDTO updatedCert = new CertificateDTO(
            1L, "ISO14001", "Bureau Veritas",
            LocalDate.now(), LocalDate.now().plusYears(1),
            "Milieu certificaat", "stored_test.pdf", 1L
        );
        when(certificateService.updateCertificaatBestand(eq(1L), eq("stored_test.pdf")))
            .thenReturn(updatedCert);

        // Voer test uit
        mockMvc.perform(multipart("/api/certificaten/{id}/bestand", 1L)
                .file(file)
                .characterEncoding("UTF-8"))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bestandsPad").value("stored_test.pdf"));
    }

    @Test
    @WithMockUser
    void whenGetAllCertificates_thenSuccess() throws Exception {
        when(certificateService.getAlleCertificaten())
            .thenReturn(Arrays.asList(testCertificaat));

        mockMvc.perform(get("/api/certificaten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].naam").value("ISO14001"));
    }

    @Test
    @WithMockUser
    void whenCreateCertificate_thenSuccess() throws Exception {
        when(certificateService.maakCertificaat(any(CertificateDTO.class)))
            .thenReturn(testCertificaat);

        mockMvc.perform(post("/api/certificaten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCertificaat)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void whenGetVerlopenCertificaten_thenSuccess() throws Exception {
        when(certificateService.getVerlopenCertificaten(any()))
            .thenReturn(Arrays.asList(testCertificaat));

        mockMvc.perform(get("/api/certificaten/verlopen")
                .param("datum", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].naam").value("ISO14001"));
    }

    @Test
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/certificaten"))
                .andExpect(status().isForbidden());
    }
}