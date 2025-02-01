// src/test/java/com/greentrade/greentrade/integration/CertificateUploadIntegrationTest.java

package com.greentrade.greentrade.integration;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.CertificateDTO;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Certificate Upload Integration Tests")
class CertificateUploadIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private ObjectMapper objectMapper;

   private CertificateDTO testCertificate;
   private MockMultipartFile testFile;

   @BeforeEach
   @SuppressWarnings("unused")
   void setUp() {
       // Test certificaat aanmaken
       testCertificate = new CertificateDTO(
           null,
           "ISO 14001", 
           "Bureau Veritas",
           LocalDate.now(),
           LocalDate.now().plusYears(1),
           "Milieucertificaat",
           null,
           2L  // ID van verkoper uit data.sql
       );

       // Test bestand aanmaken
       testFile = new MockMultipartFile(
           "bestand",
           "test-cert.pdf",
           MediaType.APPLICATION_PDF_VALUE,
           "PDF test content".getBytes()
       );
   }

    @Test
    @DisplayName("Complete certificaat upload - happy path")
    @WithMockUser(username = "verkoper@greentrade.nl", roles = "VERKOPER")
    void whenUploadCertificate_thenSuccess() throws Exception {
        // Debug: Print test certificaat
        System.out.println("Test certificaat voor creatie: " + objectMapper.writeValueAsString(testCertificate));

        // 1. Certificaat aanmaken
        MvcResult createResult = mockMvc.perform(post("/api/certificaten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCertificate)))
                .andExpect(status().isOk())
                .andDo(result -> System.out.println("Create Response: " + result.getResponse().getContentAsString()))
                .andReturn();

        CertificateDTO createdCertificate = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            CertificateDTO.class
        );

       assertNotNull(createdCertificate.getId());

       // 2. Bestand uploaden
       MvcResult uploadResult = mockMvc.perform(multipart("/api/certificaten/{id}/bestand", createdCertificate.getId())
               .file(testFile))
               .andExpect(status().isOk())
               .andReturn();

       CertificateDTO updatedCertificate = objectMapper.readValue(
           uploadResult.getResponse().getContentAsString(),
           CertificateDTO.class
       );
       assertNotNull(updatedCertificate.getBestandsPad());

       // 3. Bestand downloaden en verifiëren
       mockMvc.perform(get("/api/certificaten/{id}/bestand", updatedCertificate.getId()))
               .andExpect(status().isOk())
               .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE));
   }

   @Test
   @DisplayName("Upload ongeldig bestandstype - verwacht 400")
   @WithMockUser(roles = "VERKOPER")
   void whenUploadInvalidFile_thenBadRequest() throws Exception {
       // Eerst geldig certificaat aanmaken
       MvcResult createResult = mockMvc.perform(post("/api/certificaten")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testCertificate)))
               .andExpect(status().isOk())
               .andReturn();

       CertificateDTO createdCertificate = objectMapper.readValue(
           createResult.getResponse().getContentAsString(),
           CertificateDTO.class
       );

       // Probeer ongeldig bestand te uploaden
       MockMultipartFile invalidFile = new MockMultipartFile(
           "bestand",
           "test.exe",
           "application/octet-stream",
           "Invalid content".getBytes()
       );

       mockMvc.perform(multipart("/api/certificaten/{id}/bestand", createdCertificate.getId())
               .file(invalidFile))
               .andExpect(status().isBadRequest());
   }

   @Test
   @DisplayName("Download niet-bestaand bestand - verwacht 404")
   @WithMockUser(roles = "VERKOPER")
   void whenDownloadNonExistentFile_thenNotFound() throws Exception {
       mockMvc.perform(get("/api/certificaten/999/bestand"))
               .andExpect(status().isNotFound());
   }

   @Test
   @DisplayName("Upload zonder authenticatie - verwacht 403")
   void whenUploadWithoutAuth_thenForbidden() throws Exception {
       mockMvc.perform(multipart("/api/certificaten/1/bestand")
               .file(testFile))
               .andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("Upload te groot bestand - verwacht 400")
   @WithMockUser(roles = "VERKOPER")
   void whenUploadTooLargeFile_thenBadRequest() throws Exception {
       // Eerst geldig certificaat aanmaken
       MvcResult createResult = mockMvc.perform(post("/api/certificaten")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testCertificate)))
               .andExpect(status().isOk())
               .andReturn();

       CertificateDTO createdCertificate = objectMapper.readValue(
           createResult.getResponse().getContentAsString(),
           CertificateDTO.class
       );

       // Probeer te groot bestand te uploaden
       byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
       MockMultipartFile largeFile = new MockMultipartFile(
           "bestand",
           "large.pdf",
           MediaType.APPLICATION_PDF_VALUE,
           largeContent
       );

       mockMvc.perform(multipart("/api/certificaten/{id}/bestand", createdCertificate.getId())
               .file(largeFile))
               .andExpect(status().isBadRequest());
   }
}