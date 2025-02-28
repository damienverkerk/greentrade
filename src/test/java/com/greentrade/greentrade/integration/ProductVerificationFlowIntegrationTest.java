package com.greentrade.greentrade.integration;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.product.ProductCreateRequest;
import com.greentrade.greentrade.dto.product.ProductResponse;
import com.greentrade.greentrade.dto.verification.VerificationResponse;
import com.greentrade.greentrade.dto.verification.VerificationReviewRequest;
import com.greentrade.greentrade.models.VerificationStatus;

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

   private ProductCreateRequest createRequest;
   private VerificationReviewRequest reviewRequest;

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
   }

   @Test
   @DisplayName("Complete verification flow - happy path")
   @WithMockUser(username = "seller@greentrade.nl", roles = {"SELLER", "ADMIN"})
   void completeVerificationFlow() throws Exception {
       
       MvcResult createResult = mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

       ProductResponse createdProduct = objectMapper.readValue(
           createResult.getResponse().getContentAsString(), 
           ProductResponse.class
       );

       
       MvcResult submitResult = mockMvc.perform(post("/api/verifications/products/{id}/submit", 
               createdProduct.getId()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("PENDING"))
               .andReturn();

       VerificationResponse verification = objectMapper.readValue(
           submitResult.getResponse().getContentAsString(), 
           VerificationResponse.class
       );

      
       mockMvc.perform(post("/api/verifications/{id}/review", verification.getId())
               .param("reviewerId", "1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(reviewRequest))
               .with(request -> {
                   request.setUserPrincipal(() -> "admin@greentrade.nl");
                   return request;
               }))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("APPROVED"))
               .andExpect(jsonPath("$.sustainabilityScore").value(85));

       
       mockMvc.perform(get("/api/products/{id}", createdProduct.getId()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.sustainabilityScore").value(85));
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
       
       mockMvc.perform(post("/api/verifications/products/999/submit"))
               .andExpect(status().isNotFound());
   }

   @Test
   @DisplayName("Review without score gives 400") 
   @WithMockUser(roles = "ADMIN")
   void reviewWithoutScore_ReturnsBadRequest() throws Exception {
       
       VerificationReviewRequest invalidRequest = VerificationReviewRequest.builder()
           .status(VerificationStatus.APPROVED)
           .reviewerNotes("Missing score")
           .build();

      
       mockMvc.perform(post("/api/verifications/1/review")
               .param("reviewerId", "1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(invalidRequest)))
               .andExpect(status().isBadRequest());
   }
   
   @Test
   @DisplayName("Rejection with reason succeeds")
   @WithMockUser(roles = "ADMIN")
   void reviewWithRejection_Succeeds() throws Exception {
       
       MvcResult createResult = mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest))
            .with(request -> {
                request.setUserPrincipal(() -> "seller@greentrade.nl");
                return request;
            }))
            .andExpect(status().isCreated())
            .andReturn();

       ProductResponse createdProduct = objectMapper.readValue(
           createResult.getResponse().getContentAsString(), 
           ProductResponse.class
       );
       
       MvcResult submitResult = mockMvc.perform(post("/api/verifications/products/{id}/submit", 
               createdProduct.getId())
               .with(request -> {
                   request.setUserPrincipal(() -> "seller@greentrade.nl");
                   return request;
               }))
               .andExpect(status().isOk())
               .andReturn();

       VerificationResponse verification = objectMapper.readValue(
           submitResult.getResponse().getContentAsString(), 
           VerificationResponse.class
       );
       
      
       VerificationReviewRequest rejectionRequest = VerificationReviewRequest.builder()
           .status(VerificationStatus.REJECTED)
           .rejectionReason("Product voldoet niet aan de duurzaamheidscriteria")
           .reviewerNotes("Afgewezen vanwege materiaalgebruik")
           .build();

     
       mockMvc.perform(post("/api/verifications/{id}/review", verification.getId())
               .param("reviewerId", "1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(rejectionRequest)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("REJECTED"))
               .andExpect(jsonPath("$.rejectionReason").value("Product voldoet niet aan de duurzaamheidscriteria"));
   }
}