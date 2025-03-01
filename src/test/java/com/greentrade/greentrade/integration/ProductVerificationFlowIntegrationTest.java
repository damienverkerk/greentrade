// ProductVerificationFlowIntegrationTest.java update
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
      
      
      MvcResult createResult = mockMvc.perform(post("/api/products")
           .contentType(MediaType.APPLICATION_JSON)
           .content(objectMapper.writeValueAsString(createRequest)))
           .andExpect(status().isCreated())
           .andReturn();

      
      MvcResult submitResult = mockMvc.perform(post("/api/verifications/products/{id}/submit", 
              mockProductResponse.getId()))
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.status").value("PENDING"))
              .andReturn();

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
      
      VerificationReviewRequest invalidRequest = VerificationReviewRequest.builder()
          .status(VerificationStatus.APPROVED)
          .reviewerNotes("Missing score")
          .build();
      
      
      when(verificationService.reviewProduct(anyLong(), any(VerificationReviewRequest.class), anyLong()))
          .thenThrow(new ProductVerificationException("Sustainability score is required for approval"));
          
     
      mockMvc.perform(post("/api/verifications/1/review")
              .param("reviewerId", "1")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isBadRequest());
  }
  
  @Test
  @DisplayName("Rejection with reason succeeds")
  @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "SELLER"})
  void reviewWithRejection_Succeeds() throws Exception {
      
      when(productService.createProduct(any(ProductCreateRequest.class)))
          .thenReturn(mockProductResponse);
          
      
      when(verificationService.submitForVerification(anyLong()))
          .thenReturn(mockVerificationResponse);
      
      
      VerificationReviewRequest rejectionRequest = VerificationReviewRequest.builder()
          .status(VerificationStatus.REJECTED)
          .rejectionReason("Product voldoet niet aan de duurzaamheidscriteria")
          .reviewerNotes("Afgewezen vanwege materiaalgebruik")
          .build();

      
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

      
      mockMvc.perform(post("/api/products")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(createRequest)))
              .andExpect(status().isCreated());

      
      mockMvc.perform(post("/api/verifications/products/{id}/submit", 1L))
              .andExpect(status().isCreated());

      
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
      
      mockMvc.perform(post("/api/verifications/products/1/submit"))
              .andExpect(status().isForbidden());
  }

  @Test 
  @DisplayName("Submit non-existent product gives 404")
  @WithMockUser(roles = "SELLER")
  void submitNonExistentProduct_Returns404() throws Exception {
      
      when(verificationService.submitForVerification(eq(999L)))
          .thenThrow(new ProductNotFoundException(999L));
      
      
      mockMvc.perform(post("/api/verifications/products/999/submit"))
              .andExpect(status().isNotFound());
  }
}