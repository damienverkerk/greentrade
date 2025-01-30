package com.greentrade.greentrade.controllers;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
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
import com.greentrade.greentrade.exception.verification.DuplicateVerificationException;
import com.greentrade.greentrade.exception.verification.InvalidVerificationStatusException;
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

    private ProductVerificationDTO testVerificationDTO;
    private ProductVerificationDTO testReviewDTO;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testVerificationDTO = new ProductVerificationDTO();
        testVerificationDTO.setProductId(1L);
        testVerificationDTO.setStatus(VerificationStatus.PENDING);

        testReviewDTO = new ProductVerificationDTO();
        testReviewDTO.setProductId(1L);
        testReviewDTO.setStatus(VerificationStatus.APPROVED);
        testReviewDTO.setSustainabilityScore(85);
        testReviewDTO.setReviewerNotes("Test notes");
    }

    @Test
    @WithMockUser(roles = "VERKOPER")
    void whenSubmitVerification_thenSuccess() throws Exception {
        when(verificationService.submitForVerification(anyLong()))
            .thenReturn(testVerificationDTO);

        mockMvc.perform(post("/api/verifications/products/1/submit")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.productId").value(1L));
    }

    @Test
    @WithMockUser(roles = "VERKOPER")
    void whenSubmitVerificationWithExistingVerification_thenBadRequest() throws Exception {
        when(verificationService.submitForVerification(anyLong()))
            .thenThrow(new DuplicateVerificationException(1L));

        mockMvc.perform(post("/api/verifications/products/1/submit")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Er is al een lopende verificatie voor product met ID: 1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenReviewVerification_thenSuccess() throws Exception {
        testReviewDTO.setStatus(VerificationStatus.APPROVED);
        when(verificationService.reviewProduct(anyLong(), any(), anyLong()))
            .thenReturn(testReviewDTO);

        mockMvc.perform(post("/api/verifications/1/review")
                .param("reviewerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.sustainabilityScore").value(85));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenReviewVerificationWithoutScore_thenBadRequest() throws Exception {
        testReviewDTO.setSustainabilityScore(null);
        when(verificationService.reviewProduct(anyLong(), any(), anyLong()))
            .thenThrow(new InvalidVerificationStatusException("Duurzaamheidsscore is verplicht bij goedkeuring"));

        mockMvc.perform(post("/api/verifications/1/review")
                .param("reviewerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReviewDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duurzaamheidsscore is verplicht bij goedkeuring"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenReviewVerificationWithRejection_thenSuccess() throws Exception {
        testReviewDTO.setStatus(VerificationStatus.REJECTED);
        testReviewDTO.setSustainabilityScore(null);
        testReviewDTO.setRejectionReason("Product voldoet niet aan eisen");

        when(verificationService.reviewProduct(anyLong(), any(), anyLong()))
            .thenReturn(testReviewDTO);

        mockMvc.perform(post("/api/verifications/1/review")
                .param("reviewerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").exists())
                .andExpect(jsonPath("$.sustainabilityScore").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenGetPendingVerifications_thenSuccess() throws Exception {
        when(verificationService.getPendingVerifications())
            .thenReturn(Arrays.asList(testVerificationDTO));

        mockMvc.perform(get("/api/verifications/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].productId").value(1L));
    }

    @Test
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/verifications/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "KOPER")
    void whenInvalidRole_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/verifications/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VERKOPER")
    void whenGetVerificationsByProduct_thenSuccess() throws Exception {
        when(verificationService.getVerificationsByProduct(anyLong()))
            .thenReturn(Arrays.asList(testVerificationDTO));

        mockMvc.perform(get("/api/verifications/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].productId").value(1L));
    }
}