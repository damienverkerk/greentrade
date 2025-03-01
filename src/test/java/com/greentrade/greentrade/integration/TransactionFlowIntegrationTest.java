package com.greentrade.greentrade.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

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
import com.greentrade.greentrade.services.ProductService;
import com.greentrade.greentrade.services.TransactionService;

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
   
   @MockBean
   private ProductService productService;
   
   @MockBean
   private TransactionService transactionService;

   private ProductCreateRequest productRequest;
   private TransactionCreateRequest transactionRequest;
   private ProductResponse mockProductResponse;
   private TransactionResponse mockTransactionResponse;

   @BeforeEach
   void setUp() {
       // Setup test data
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
           .productId(1L)
           .amount(new BigDecimal("299.99"))
           .build();
           
       // Create mock responses
       mockProductResponse = ProductResponse.builder()
           .id(1L)
           .name("Duurzame Stoel")
           .description("Test product")
           .price(new BigDecimal("299.99"))
           .sustainabilityScore(85)
           .sustainabilityCertificate("ISO14001")
           .sellerId(2L)
           .build();
           
       mockTransactionResponse = TransactionResponse.builder()
           .id(1L)
           .buyerId(3L)
           .productId(1L)
           .amount(new BigDecimal("299.99"))
           .date(LocalDateTime.now())
           .status("PENDING")
           .build();
   }

   @Test
   @DisplayName("Complete transaction flow - happy path")
   @WithMockUser(roles = {"SELLER", "BUYER"})
   void completeTransactionFlow() throws Exception {
       // Mock service responses
       when(productService.createProduct(any(ProductCreateRequest.class)))
           .thenReturn(mockProductResponse);
       
       when(transactionService.createTransaction(any(TransactionCreateRequest.class)))
           .thenReturn(mockTransactionResponse);
           
       TransactionResponse completedTransaction = TransactionResponse.builder()
           .id(1L)
           .buyerId(3L)
           .productId(1L)
           .amount(new BigDecimal("299.99"))
           .date(LocalDateTime.now())
           .status("COMPLETED")
           .build();
           
       when(transactionService.updateTransactionStatus(anyLong(), eq("COMPLETED")))
           .thenReturn(completedTransaction);
           
       when(transactionService.getTransactionsByBuyer(anyLong()))
           .thenReturn(Arrays.asList(completedTransaction));
       
       // Create product
       MvcResult createProductResult = mockMvc.perform(post("/api/products")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(productRequest)))
               .andExpect(status().isCreated())
               .andReturn();

       // Create transaction
       MvcResult createTransactionResult = mockMvc.perform(post("/api/transactions")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(transactionRequest)))
               .andExpect(status().isCreated())
               .andReturn();

       // Update transaction status
       mockMvc.perform(put("/api/transactions/{id}/status", 1L)
               .param("newStatus", "COMPLETED"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("COMPLETED"));

       // Get buyer transactions
       mockMvc.perform(get("/api/transactions/buyer/{buyerId}", 3L))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].status").value("COMPLETED"));
   }

   @Test
   @DisplayName("Transaction with invalid amount gives 400")
   @WithMockUser(roles = "BUYER")
   void createTransactionWithInvalidAmount_ReturnsBadRequest() throws Exception {
       // Create an invalid transaction request with negative amount
       TransactionCreateRequest invalidRequest = TransactionCreateRequest.builder()
           .buyerId(3L)
           .productId(1L)
           .amount(new BigDecimal("-100.00"))
           .build();
       
       // Mock service to throw exception
       when(transactionService.createTransaction(any(TransactionCreateRequest.class)))
           .thenThrow(new IllegalArgumentException("Amount must be greater than 0"));
           
       // Try to create transaction with invalid amount
       mockMvc.perform(post("/api/transactions")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(invalidRequest)))
               .andExpect(status().isBadRequest());
   }

   @Test
   @DisplayName("Update transaction status without auth gives 403") 
   void updateStatusWithoutAuth_ReturnsForbidden() throws Exception {
       // Try to update status without authentication
       mockMvc.perform(put("/api/transactions/1/status")
               .param("newStatus", "COMPLETED"))
               .andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("Get transactions with wrong role gives 403")
   @WithMockUser(roles = "SELLER")
   void getTransactionAsWrongRole_ReturnsForbidden() throws Exception {
       // Try to access buyer transactions as seller
       mockMvc.perform(get("/api/transactions/buyer/1"))
               .andExpect(status().isForbidden());
   }
   
   @Test
   @DisplayName("Get transactions between dates")
   @WithMockUser
   void getTransactionsBetweenDates_ReturnsTransactions() throws Exception {
       // Setup dates
       LocalDateTime start = LocalDateTime.now().minusDays(30);
       LocalDateTime end = LocalDateTime.now();
       
       // Mock service response
       when(transactionService.getTransactionsBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class)))
           .thenReturn(Arrays.asList(mockTransactionResponse));
       
       // Get transactions between dates
       mockMvc.perform(get("/api/transactions/period")
               .param("start", start.toString())
               .param("end", end.toString()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].status").value("PENDING"));
   }
}