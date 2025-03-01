package com.greentrade.greentrade.controllers;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.verification.VerificationResponse;
import com.greentrade.greentrade.dto.verification.VerificationReviewRequest;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
import com.greentrade.greentrade.exception.verification.DuplicateVerificationException;
import com.greentrade.greentrade.exception.verification.InvalidVerificationStatusException;
import com.greentrade.greentrade.models.VerificationStatus;
import com.greentrade.greentrade.services.ProductVerificationService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
class ProductVerificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductVerificationService verificationService;

    private VerificationResponse pendingVerification;
    private VerificationReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        
        pendingVerification = VerificationResponse.builder()
            .id(1L)
            .productId(1L)
            .status(VerificationStatus.PENDING)
            .build();

        
        reviewRequest = VerificationReviewRequest.builder()
            .status(VerificationStatus.APPROVED)
            .sustainabilityScore(85)
            .reviewerNotes("Test notes")
            .build();
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void whenSubmitVerification_thenSuccess() throws Exception {
        
        when(verificationService.submitForVerification(anyLong()))
            .thenReturn(pendingVerification);

        
        mockMvc.perform(post("/api/verifications/products/1/submit")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.productId").value(1L));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void whenSubmitVerificationWithExistingVerification_thenConflict() throws Exception {
        
        when(verificationService.submitForVerification(anyLong()))
            .thenThrow(new DuplicateVerificationException(1L));

        
        mockMvc.perform(post("/api/verifications/products/1/submit")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Er is al een lopende verificatie voor product met ID: 1"));
    }
    
    @Test
    @WithMockUser(roles = "SELLER")
    void whenSubmitVerificationWithNonExistingProduct_thenNotFound() throws Exception {
        
        when(verificationService.submitForVerification(eq(999L)))
            .thenThrow(new ProductNotFoundException(999L));

        
        mockMvc.perform(post("/api/verifications/products/999/submit")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenReviewVerification_thenSuccess() throws Exception {
        
        VerificationResponse approvedVerification = VerificationResponse.builder()
            .id(1L)
            .productId(1L)
            .status(VerificationStatus.APPROVED)
            .sustainabilityScore(85)
            .reviewerId(1L)
            .reviewerNotes("Test notes")
            .build();
            
        when(verificationService.reviewProduct(anyLong(), any(), anyLong()))
            .thenReturn(approvedVerification);

        
        mockMvc.perform(post("/api/verifications/1/review")
                .param("reviewerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.sustainabilityScore").value(85));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenReviewVerificationWithoutScore_thenBadRequest() throws Exception {
        
        reviewRequest.setSustainabilityScore(null);
        when(verificationService.reviewProduct(anyLong(), any(), anyLong()))
            .thenThrow(new InvalidVerificationStatusException("Sustainability score is required for approval"));

        
        mockMvc.perform(post("/api/verifications/1/review")
                .param("reviewerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Sustainability score is required for approval"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenReviewVerificationWithRejection_thenSuccess() throws Exception {
        
        reviewRequest.setStatus(VerificationStatus.REJECTED);
        reviewRequest.setSustainabilityScore(null);
        reviewRequest.setRejectionReason("Product does not meet requirements");

        VerificationResponse rejectedVerification = VerificationResponse.builder()
            .id(1L)
            .productId(1L)
            .status(VerificationStatus.REJECTED)
            .rejectionReason("Product does not meet requirements")
            .reviewerId(1L)
            .reviewerNotes("Test notes")
            .build();
            
        when(verificationService.reviewProduct(anyLong(), any(), anyLong()))
            .thenReturn(rejectedVerification);

        
        mockMvc.perform(post("/api/verifications/1/review")
                .param("reviewerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenGetPendingVerifications_thenSuccess() throws Exception {
        
        when(verificationService.getPendingVerifications())
            .thenReturn(Arrays.asList(pendingVerification));

        
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
    @WithMockUser(roles = "BUYER")
    void whenInvalidRole_thenForbidden() throws Exception {
        
        mockMvc.perform(get("/api/verifications/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void whenGetVerificationsByProduct_thenSuccess() throws Exception {
        
        when(verificationService.getVerificationsByProduct(anyLong()))
            .thenReturn(Arrays.asList(pendingVerification));

        mockMvc.perform(get("/api/verifications/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].productId").value(1L));
    }
}