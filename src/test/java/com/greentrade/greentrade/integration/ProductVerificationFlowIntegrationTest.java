// src/test/java/com/greentrade/greentrade/integration/ProductVerificationFlowIntegrationTest.java

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.ProductDTO;
import com.greentrade.greentrade.dto.ProductVerificationDTO;
import com.greentrade.greentrade.models.VerificationStatus;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Product Verification Flow Integration Tests")
class ProductVerificationFlowIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private ObjectMapper objectMapper;

   private ProductDTO testProduct;

   @BeforeEach
   @SuppressWarnings("unused")
   void setUp() {
       // Arrange: Test product aanmaken
       testProduct = new ProductDTO(
           null,
           "Duurzame Bureaustoel", 
           "Ergonomische bureaustoel van gerecycled materiaal",
           new BigDecimal("299.99"),
           null,
           null,
           2L
       );
   }

   @Test
   @DisplayName("Complete verification flow - happy path")
   @WithMockUser(username = "verkoper@test.nl", roles = {"ROLE_SELLER", "ROLE_ADMIN"})
   void completeVerificationFlow() throws Exception {
       // Arrange: Product aanmaken
       MvcResult createResult = mockMvc.perform(post("/api/producten")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testProduct)))
            .andDo(result -> System.out.println("Create Response: " + result.getResponse().getContentAsString()))
            .andExpect(status().isCreated())
            .andReturn();

       ProductDTO createdProduct = objectMapper.readValue(
           createResult.getResponse().getContentAsString(), 
           ProductDTO.class
       );

       // Act & Assert: Verificatie aanvragen
       MvcResult submitResult = mockMvc.perform(post("/api/verifications/products/{id}/submit", 
               createdProduct.getId()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("PENDING"))
               .andReturn();

       ProductVerificationDTO verification = objectMapper.readValue(
           submitResult.getResponse().getContentAsString(), 
           ProductVerificationDTO.class
       );

       // Act & Assert: Admin keurt goed
       verification.setStatus(VerificationStatus.APPROVED);
       verification.setSustainabilityScore(85);
       verification.setReviewerNotes("Product voldoet aan duurzaamheidscriteria");

       mockMvc.perform(post("/api/verifications/{id}/review", verification.getId())
               .param("reviewerId", "1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(verification))
               .with(request -> {
                   request.setUserPrincipal(() -> "admin@greentrade.nl");
                   return request;
               }))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("APPROVED"))
               .andExpect(jsonPath("$.sustainabilityScore").value(85));

       // Assert: Verifieer eindresultaat
       mockMvc.perform(get("/api/producten/{id}", createdProduct.getId()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.duurzaamheidsScore").value(85));
   }

   @Test
   @DisplayName("Submit zonder authenticatie geeft 403")
   void submitWithoutAuth_ReturnsForbidden() throws Exception {
       mockMvc.perform(post("/api/verifications/products/1/submit"))
               .andExpect(status().isForbidden());
   }

   @Test 
   @DisplayName("Submit niet-bestaand product geeft 404")
   @WithMockUser(roles = "VERKOPER")
   void submitNonExistentProduct_Returns404() throws Exception {
       mockMvc.perform(post("/api/verifications/products/999/submit"))
               .andExpect(status().isNotFound());
   }

   @Test
   @DisplayName("Review zonder score geeft 400") 
   @WithMockUser(roles = "ADMIN")
   void reviewWithoutScore_ReturnsBadRequest() throws Exception {
       ProductVerificationDTO dto = new ProductVerificationDTO();
       dto.setStatus(VerificationStatus.APPROVED);

       mockMvc.perform(post("/api/verifications/1/review")
               .param("reviewerId", "1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(dto)))
               .andExpect(status().isBadRequest());
   }
}