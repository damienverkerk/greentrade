package com.greentrade.greentrade.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.transaction.TransactionCreateRequest;
import com.greentrade.greentrade.dto.transaction.TransactionResponse;
import com.greentrade.greentrade.services.TransactionService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private TransactionResponse testTransaction;
    private TransactionCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        
        testTransaction = TransactionResponse.builder()
            .id(1L)
            .buyerId(1L)
            .productId(1L)
            .amount(new BigDecimal("299.99"))
            .date(LocalDateTime.now())
            .status("PROCESSING")
            .build();
            
       
        createRequest = TransactionCreateRequest.builder()
            .buyerId(1L)
            .productId(1L)
            .amount(new BigDecimal("299.99"))
            .build();
    }

    @Test
    @WithMockUser
    void whenGetAllTransactions_thenSuccess() throws Exception {
        
        when(transactionService.getAllTransactions())
            .thenReturn(Arrays.asList(testTransaction));

       
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PROCESSING"))
                .andExpect(jsonPath("$[0].amount").value(299.99));
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void whenCreateTransaction_thenSuccess() throws Exception {
        
        when(transactionService.createTransaction(any(TransactionCreateRequest.class)))
            .thenReturn(testTransaction);

        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @WithMockUser
    void whenUpdateTransactionStatus_thenSuccess() throws Exception {
        
        TransactionResponse updatedTransaction = TransactionResponse.builder()
            .id(1L)
            .buyerId(1L)
            .productId(1L)
            .amount(new BigDecimal("299.99"))
            .date(LocalDateTime.now())
            .status("COMPLETED")
            .build();

        when(transactionService.updateTransactionStatus(anyLong(), any()))
            .thenReturn(updatedTransaction);

        
        mockMvc.perform(put("/api/transactions/{id}/status", 1L)
                .param("newStatus", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "BUYER") 
    void whenGetTransactionsByBuyer_thenSuccess() throws Exception {
        
        when(transactionService.getTransactionsByBuyer(anyLong()))
            .thenReturn(Arrays.asList(testTransaction));
    
        
        mockMvc.perform(get("/api/transactions/buyer/{buyerId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buyerId").value(1));
    }

    @Test
    @WithMockUser
    void whenGetTransactionsBySeller_thenSuccess() throws Exception {
        
        when(transactionService.getTransactionsBySeller(anyLong()))
            .thenReturn(Arrays.asList(testTransaction));

        
        mockMvc.perform(get("/api/transactions/seller/{sellerId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buyerId").value(1));
    }

    @Test
    @WithMockUser
    void whenGetTransactionsBetweenDates_thenSuccess() throws Exception {
        
        when(transactionService.getTransactionsBetweenDates(any(), any()))
            .thenReturn(Arrays.asList(testTransaction));

        
        mockMvc.perform(get("/api/transactions/period")
                .param("start", LocalDateTime.now().minusDays(7).toString())
                .param("end", LocalDateTime.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PROCESSING"));
    }

    @Test
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isForbidden());
    }
}