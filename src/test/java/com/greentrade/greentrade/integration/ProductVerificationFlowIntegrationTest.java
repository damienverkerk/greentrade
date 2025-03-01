package com.greentrade.greentrade.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.product.ProductCreateRequest;
import com.greentrade.greentrade.dto.product.ProductResponse;
import com.greentrade.greentrade.dto.verification.VerificationResponse;
import com.greentrade.greentrade.dto.verification.VerificationReviewRequest;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
import com.greentrade.greentrade.exception.verification.ProductVerificationException;
import com.greentrade.greentrade.models.VerificationStatus;
import com.greentrade.greentrade.services.ProductService;
import com.greentrade.greentrade.services.ProductVerificationService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
@DisplayName("Product Verification Flow Integration Tests")
class ProductVerificationFlowIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private ObjectMapper objectMapper;
   
   @MockBean
   private ProductService productService;
   
   @MockBean
   private ProductVerificationService verificationService;

   private ProductCreateRequest createRequest;
   private VerificationReviewRequest reviewRequest;
   private ProductResponse mockProductResponse;
   private VerificationResponse mockVerificationResponse;

   @BeforeEach
   void setUp() {
       // Setup test data
       createRequest = ProductCreateRequest.builder()
           .name("Duurzame Bureaustoel")
           .description("Ergonomische bureaustoel van gerecycled materiaal")
           .price(new BigDecimal("299.99"))
           .sellerId(2L)
           .build();
        
       reviewRequest = VerificationReviewRequest.builder()
           .status(VerificationStatus.APPROVED)
           .sustainabilityScore(85)
           .reviewerNotes("Product voldoet aan duurzaamheidscriteria")
           .build();
           
       // Create mock responses
       mockProductResponse = ProductResponse.builder()
           .id(1L)
           .name("Duurzame Bureaustoel")
           .description("Ergonomische bureaustoel van gerecycled materiaal")
           .price(new BigDecimal("299.99"))
           .sellerId(2L)
           .build();
           
       mockVerificationResponse = VerificationResponse.builder()
           .id(1L)
           .productId(1L)
           .status(VerificationStatus.PENDING)
           .submissionDate(LocalDateTime.now())
           .build();
   }

   @Test
   @DisplayName("Complete verification flow - happy path")
   @WithMockUser(username = "seller@greentrade.nl", roles = {"SELLER", "ADMIN"})
   void completeVerificationFlow() throws Exception {
       // Mock service responses
       when(productService.createProduct(any(ProductCreateRequest.class)))
           .thenReturn(mockProductResponse);
       
       when(verificationService.submitForVerification(anyLong()))
           .thenReturn(mockVerificationResponse);
           
       VerificationResponse approvedResponse = VerificationResponse.builder()
           .id(1L)
           .productId(1L)
           .status(VerificationStatus.APPROVED)
           .reviewerId(1L)
           .sustainabilityScore(85)
           .reviewerNotes("Product voldoet aan duurzaamheidscriteria")
           .build();
           
       when(verificationService.reviewProduct(anyLong(), any(VerificationReviewRequest.class), anyLong()))
           .thenReturn(approvedResponse);
       
       // Create product
       MvcResult createResult = mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

       // Submit for verification
       MvcResult submitResult = mockMvc.perform(post("/api/verifications/products/{id}/submit", 
               mockProductResponse.getId()))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.status").value("PENDING"))
               .andReturn();

       // Review verification
       mockMvc.perform(post("/api/verifications/{id}/review", mockVerificationResponse.getId())
               .param("reviewerId", "1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(reviewRequest)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("APPROVED"))
               .andExpect(jsonPath("$.sustainabilityScore").value(85));
   }

   @Test
   @DisplayName("Review without score gives 400") 
   @WithMockUser(roles = "ADMIN")
   void reviewWithoutScore_ReturnsBadRequest() throws Exception {
       // Setup invalid request (missing score)
       VerificationReviewRequest invalidRequest = VerificationReviewRequest.builder()
           .status(VerificationStatus.APPROVED)
           .reviewerNotes("Missing score")
           .build();
       
       // Mock behavior to throw exception for missing score
       when(verificationService.reviewProduct(anyLong(), any(VerificationReviewRequest.class), anyLong()))
           .thenThrow(new ProductVerificationException("Sustainability score is required for approval"));
           
       // Test the endpoint
       mockMvc.perform(post("/api/verifications/1/review")
               .param("reviewerId", "1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(invalidRequest)))
               .andExpect(status().isBadRequest());
   }
   
   @Test
   @DisplayName("Rejection with reason succeeds")
   @WithMockUser(username = "admin@example.com", roles = "ADMIN")
   void reviewWithRejection_Succeeds() throws Exception {
       // Mock product creation
       when(productService.createProduct(any(ProductCreateRequest.class)))
           .thenReturn(mockProductResponse);
           
       // Mock verification submission
       when(verificationService.submitForVerification(anyLong()))
           .thenReturn(mockVerificationResponse);
       
       // Setup rejection request
       VerificationReviewRequest rejectionRequest = VerificationReviewRequest.builder()
           .status(VerificationStatus.REJECTED)
           .rejectionReason("Product voldoet niet aan de duurzaamheidscriteria")
           .reviewerNotes("Afgewezen vanwege materiaalgebruik")
           .build();

       // Mock response for rejection
       VerificationResponse rejectedResponse = VerificationResponse.builder()
           .id(1L)
           .productId(1L)
           .status(VerificationStatus.REJECTED)
           .rejectionReason("Product voldoet niet aan de duurzaamheidscriteria")
           .reviewerId(1L)
           .reviewerNotes("Afgewezen vanwege materiaalgebruik")
           .build();
           
       when(verificationService.reviewProduct(anyLong(), any(VerificationReviewRequest.class), anyLong()))
           .thenReturn(rejectedResponse);

       // First create a product with seller role
       mockMvc.perform(post("/api/products")
               .with(request -> {
                   request.setAttribute("SPRING_SECURITY_CONTEXT", org.springframework.security.core.context.SecurityContextHolder.createEmptyContext());
                   return request;
               })
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(createRequest)))
               .andExpect(status().isCreated());

       // Submit for verification
       mockMvc.perform(post("/api/verifications/products/{id}/submit", 1L)
               .with(request -> {
                   request.setAttribute("SPRING_SECURITY_CONTEXT", org.springframework.security.core.context.SecurityContextHolder.createEmptyContext());
                   return request;
               }))
               .andExpect(status().isCreated());

       // Review and reject verification
       mockMvc.perform(post("/api/verifications/{id}/review", 1L)
               .param("reviewerId", "1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(rejectionRequest)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("REJECTED"))
               .andExpect(jsonPath("$.rejectionReason").value("Product voldoet niet aan de duurzaamheidscriteria"));
   }
   
   @Test
   @DisplayName("Submit without authentication gives 403")
   void submitWithoutAuth_ReturnsForbidden() throws Exception {
       // Try to submit without auth
       mockMvc.perform(post("/api/verifications/products/1/submit"))
               .andExpect(status().isForbidden());
   }

   @Test 
   @DisplayName("Submit non-existent product gives 404")
   @WithMockUser(roles = "SELLER")
   void submitNonExistentProduct_Returns404() throws Exception {
       // Mock ProductNotFoundException being thrown
       when(verificationService.submitForVerification(eq(999L)))
           .thenThrow(new ProductNotFoundException(999L));
       
       // Try to submit non-existent product
       mockMvc.perform(post("/api/verifications/products/999/submit"))
               .andExpect(status().isNotFound());
   }
}