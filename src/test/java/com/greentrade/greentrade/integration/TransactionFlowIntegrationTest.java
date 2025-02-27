package com.greentrade.greentrade.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.ProductDTO;
import com.greentrade.greentrade.dto.TransactionDTO;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Transaction Flow Integration Tests")
class TransactionFlowIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private ObjectMapper objectMapper;

   private ProductDTO testProduct;
   private TransactionDTO testTransaction;

   @BeforeEach
   @SuppressWarnings("unused")
   void setUp() {
       testProduct = new ProductDTO(
           null,
           "Duurzame Stoel",
           "Test product",
           new BigDecimal("299.99"),
           85,
           "ISO14001",
           2L
       );

       testTransaction = new TransactionDTO(
           null,
           3L, // koper_id
           null, // product_id wordt later ingevuld
           new BigDecimal("299.99"),
           LocalDateTime.now(),
           "IN_BEHANDELING"
       );
   }

   @Test
   @DisplayName("Complete transaction flow - happy path")
   @WithMockUser(roles = "BUYER")
   void completeTransactionFlow() throws Exception {
       // Arrange: Product aanmaken
       MvcResult createProductResult = mockMvc.perform(post("/api/producten")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testProduct))
               .with(request -> {
                   request.setUserPrincipal(() -> "verkoper@greentrade.nl");
                   return request;
               }))
               .andExpect(status().isCreated())
               .andReturn();

       ProductDTO createdProduct = objectMapper.readValue(
           createProductResult.getResponse().getContentAsString(),
           ProductDTO.class
       );
       testTransaction.setProductId(createdProduct.getId());

       // Act & Assert: Transactie aanmaken
       MvcResult createTransactionResult = mockMvc.perform(post("/api/transacties")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testTransaction)))
               .andExpect(status().isCreated())
               .andReturn();

       TransactionDTO createdTransaction = objectMapper.readValue(
           createTransactionResult.getResponse().getContentAsString(),
           TransactionDTO.class
       );

       // Act & Assert: Status updaten
       mockMvc.perform(put("/api/transacties/{id}/status", createdTransaction.getId())
               .param("nieuweStatus", "VOLTOOID"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("VOLTOOID"));

       // Assert: Transactie ophalen
       mockMvc.perform(get("/api/transacties/koper/{koperId}", 3L))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].status").value("VOLTOOID"));
   }

   @Test
   @DisplayName("Transactie met ongeldig bedrag geeft 400")
   @WithMockUser(roles = "BUYER")
   void createTransactionWithInvalidAmount_ReturnsBadRequest() throws Exception {
       testTransaction.setAmount(new BigDecimal("-100.00"));

       mockMvc.perform(post("/api/transacties")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testTransaction)))
               .andExpect(status().isBadRequest());
   }

   @Test
   @DisplayName("Transactiestatus updaten zonder auth geeft 403") 
   void updateStatusWithoutAuth_ReturnsForbidden() throws Exception {
       mockMvc.perform(put("/api/transacties/1/status")
               .param("nieuweStatus", "VOLTOOID"))
               .andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("Transactie ophalen met verkeerde rol geeft 403")
   @WithMockUser(roles = "SELLER")
   void getTransactionAsWrongRole_ReturnsForbidden() throws Exception {
       mockMvc.perform(get("/api/transacties/koper/1"))
               .andExpect(status().isForbidden());
   }
}