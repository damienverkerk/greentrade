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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.product.ProductCreateRequest;
import com.greentrade.greentrade.dto.product.ProductResponse;
import com.greentrade.greentrade.dto.transaction.TransactionCreateRequest;
import com.greentrade.greentrade.dto.transaction.TransactionResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
@DisplayName("Transaction Flow Integration Tests")
class TransactionFlowIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private ObjectMapper objectMapper;

   private ProductCreateRequest productRequest;
   private TransactionCreateRequest transactionRequest;

   @BeforeEach
   void setUp() {
       
       productRequest = ProductCreateRequest.builder()
           .name("Duurzame Stoel")
           .description("Test product")
           .price(new BigDecimal("299.99"))
           .sustainabilityScore(85)
           .sustainabilityCertificate("ISO14001")
           .sellerId(2L)
           .build();

      
       transactionRequest = TransactionCreateRequest.builder()
           .buyerId(3L)
           .amount(new BigDecimal("299.99"))
           .build();
   }

   @Test
   @DisplayName("Complete transaction flow - happy path")
   @WithMockUser(roles = {"SELLER", "BUYER"})
   void completeTransactionFlow() throws Exception {
       
       MvcResult createProductResult = mockMvc.perform(post("/api/products")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(productRequest))
               .with(request -> {
                   request.setUserPrincipal(() -> "seller@greentrade.nl");
                   return request;
               }))
               .andExpect(status().isCreated())
               .andReturn();

       ProductResponse createdProduct = objectMapper.readValue(
           createProductResult.getResponse().getContentAsString(),
           ProductResponse.class
       );
       
       
       transactionRequest.setProductId(createdProduct.getId());

      
       MvcResult createTransactionResult = mockMvc.perform(post("/api/transactions")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(transactionRequest)))
               .andExpect(status().isCreated())
               .andReturn();

       TransactionResponse createdTransaction = objectMapper.readValue(
           createTransactionResult.getResponse().getContentAsString(),
           TransactionResponse.class
       );

       
       mockMvc.perform(put("/api/transactions/{id}/status", createdTransaction.getId())
               .param("newStatus", "COMPLETED"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("COMPLETED"));

       
       mockMvc.perform(get("/api/transactions/buyer/{buyerId}", 3L))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].status").value("COMPLETED"));
   }

   @Test
   @DisplayName("Transaction with invalid amount gives 400")
   @WithMockUser(roles = "BUYER")
   void createTransactionWithInvalidAmount_ReturnsBadRequest() throws Exception {
      
       transactionRequest.setAmount(new BigDecimal("-100.00"));
       transactionRequest.setProductId(1L); // Existing product ID

       
       mockMvc.perform(post("/api/transactions")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(transactionRequest)))
               .andExpect(status().isBadRequest());
   }

   @Test
   @DisplayName("Update transaction status without auth gives 403") 
   void updateStatusWithoutAuth_ReturnsForbidden() throws Exception {
       
       mockMvc.perform(put("/api/transactions/1/status")
               .param("newStatus", "COMPLETED"))
               .andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("Get transactions with wrong role gives 403")
   @WithMockUser(roles = "SELLER")
   void getTransactionAsWrongRole_ReturnsForbidden() throws Exception {
       
       mockMvc.perform(get("/api/transactions/buyer/1"))
               .andExpect(status().isForbidden());
   }
   
   @Test
   @DisplayName("Get transactions between dates")
   @WithMockUser
   void getTransactionsBetweenDates_ReturnsTransactions() throws Exception {
      
       LocalDateTime start = LocalDateTime.now().minusDays(30);
       LocalDateTime end = LocalDateTime.now();
       
       
       mockMvc.perform(get("/api/transactions/period")
               .param("start", start.toString())
               .param("end", end.toString()))
               .andExpect(status().isOk());
   }
}