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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.certificate.CertificateCreateRequest;
import com.greentrade.greentrade.dto.certificate.CertificateResponse;

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

   private CertificateCreateRequest createRequest;
   private MockMultipartFile testFile;

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
        
        MvcResult createResult = mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CertificateResponse createdCertificate = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            CertificateResponse.class
        );

       assertNotNull(createdCertificate.getId());

      
       MvcResult uploadResult = mockMvc.perform(multipart("/api/certificates/{id}/file", createdCertificate.getId())
               .file(testFile))
               .andExpect(status().isOk())
               .andReturn();

       CertificateResponse updatedCertificate = objectMapper.readValue(
           uploadResult.getResponse().getContentAsString(),
           CertificateResponse.class
       );
       assertNotNull(updatedCertificate.getFilePath());

       
       mockMvc.perform(get("/api/certificates/{id}/file", updatedCertificate.getId()))
               .andExpect(status().isOk())
               .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE));
   }

   @Test
   @DisplayName("Upload invalid file type - expect 400")
   @WithMockUser(roles = "SELLER")
   void whenUploadInvalidFile_thenBadRequest() throws Exception {
       
       MvcResult createResult = mockMvc.perform(post("/api/certificates")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(createRequest)))
               .andExpect(status().isCreated())
               .andReturn();

       CertificateResponse createdCertificate = objectMapper.readValue(
           createResult.getResponse().getContentAsString(),
           CertificateResponse.class
       );

      
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
}