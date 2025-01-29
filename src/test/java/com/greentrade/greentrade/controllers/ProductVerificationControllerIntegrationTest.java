package com.greentrade.greentrade.controllers;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.ProductVerificationDTO;
import com.greentrade.greentrade.models.VerificationStatus;
import com.greentrade.greentrade.services.ProductVerificationService;

@SpringBootTest
@AutoConfigureMockMvc
class ProductVerificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductVerificationService verificationService;

    @Test
    @WithMockUser(roles = "VERKOPER")
    void whenSubmitVerification_thenSuccess() throws Exception {
        ProductVerificationDTO verificationDTO = new ProductVerificationDTO();
        verificationDTO.setStatus(VerificationStatus.PENDING);
        
        when(verificationService.submitForVerification(anyLong()))
            .thenReturn(verificationDTO);

        mockMvc.perform(post("/api/verifications/products/1/submit")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenReviewVerification_thenSuccess() throws Exception {
        ProductVerificationDTO reviewDTO = new ProductVerificationDTO();
        reviewDTO.setProductId(1L);
        reviewDTO.setStatus(VerificationStatus.APPROVED);
        reviewDTO.setSustainabilityScore(85);
        reviewDTO.setReviewerNotes("Test notes");

        ProductVerificationDTO responseDTO = new ProductVerificationDTO();
        responseDTO.setStatus(VerificationStatus.APPROVED);
        responseDTO.setSustainabilityScore(85);

        when(verificationService.reviewProduct(anyLong(), any(), anyLong()))
            .thenReturn(responseDTO);

        mockMvc.perform(post("/api/verifications/1/review")
                .param("reviewerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenGetPendingVerifications_thenSuccess() throws Exception {
        ProductVerificationDTO verificationDTO = new ProductVerificationDTO();
        verificationDTO.setStatus(VerificationStatus.PENDING);

        when(verificationService.getPendingVerifications())
            .thenReturn(Arrays.asList(verificationDTO));

        mockMvc.perform(get("/api/verifications/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/verifications/pending"))
                .andExpect(status().isForbidden());
    }
}