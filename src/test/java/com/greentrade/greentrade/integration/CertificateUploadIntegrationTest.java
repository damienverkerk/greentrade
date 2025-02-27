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
       // Create test certificate
       testCertificate = new CertificateDTO(
           null,
           "ISO 14001", 
           "Bureau Veritas",
           LocalDate.now(),
           LocalDate.now().plusYears(1),
           "Environmental certificate",
           null,
           2L  // ID of seller from data.sql
       );

       // Create test file
       testFile = new MockMultipartFile(
           "file",
           "test-cert.pdf",
           MediaType.APPLICATION_PDF_VALUE,
           "PDF test content".getBytes()
       );
   }

    @Test
    @DisplayName("Complete certificate upload - happy path")
    @WithMockUser(username = "seller@greentrade.nl", roles = "SELLER")
    void whenUploadCertificate_thenSuccess() throws Exception {
        // Debug: Print test certificate
        System.out.println("Test certificate before creation: " + objectMapper.writeValueAsString(testCertificate));

        // 1. Create certificate
        MvcResult createResult = mockMvc.perform(post("/api/certificates")
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

       // 2. Upload file
       MvcResult uploadResult = mockMvc.perform(multipart("/api/certificates/{id}/file", createdCertificate.getId())
               .file(testFile))
               .andExpect(status().isOk())
               .andReturn();

       CertificateDTO updatedCertificate = objectMapper.readValue(
           uploadResult.getResponse().getContentAsString(),
           CertificateDTO.class
       );
       assertNotNull(updatedCertificate.getFilePath());

       // 3. Download and verify file
       mockMvc.perform(get("/api/certificates/{id}/file", updatedCertificate.getId()))
               .andExpect(status().isOk())
               .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE));
   }

   @Test
   @DisplayName("Upload invalid file type - expect 400")
   @WithMockUser(roles = "SELLER")
   void whenUploadInvalidFile_thenBadRequest() throws Exception {
       // First create valid certificate
       MvcResult createResult = mockMvc.perform(post("/api/certificates")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testCertificate)))
               .andExpect(status().isOk())
               .andReturn();

       CertificateDTO createdCertificate = objectMapper.readValue(
           createResult.getResponse().getContentAsString(),
           CertificateDTO.class
       );

       // Try to upload invalid file
       MockMultipartFile invalidFile = new MockMultipartFile(
           "file",
           "test.exe",
           "application/octet-stream",
           "Invalid content".getBytes()
       );

       mockMvc.perform(multipart("/api/certificates/{id}/file", createdCertificate.getId())
               .file(invalidFile))
               .andExpect(status().isBadRequest());
   }

   @Test
   @DisplayName("Download non-existent file - expect 404")
   @WithMockUser(roles = "SELLER")
   void whenDownloadNonExistentFile_thenNotFound() throws Exception {
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

   @Test
   @DisplayName("Upload file too large - expect 400")
   @WithMockUser(roles = "SELLER")
   void whenUploadTooLargeFile_thenBadRequest() throws Exception {
       // First create valid certificate
       MvcResult createResult = mockMvc.perform(post("/api/certificates")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testCertificate)))
               .andExpect(status().isOk())
               .andReturn();

       CertificateDTO createdCertificate = objectMapper.readValue(
           createResult.getResponse().getContentAsString(),
           CertificateDTO.class
       );

       // Try to upload too large file
       byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
       MockMultipartFile largeFile = new MockMultipartFile(
           "file",
           "large.pdf",
           MediaType.APPLICATION_PDF_VALUE,
           largeContent
       );

       mockMvc.perform(multipart("/api/certificates/{id}/file", createdCertificate.getId())
               .file(largeFile))
               .andExpect(status().isBadRequest());
   }
}