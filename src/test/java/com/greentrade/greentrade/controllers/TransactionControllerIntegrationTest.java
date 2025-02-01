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
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.TransactionDTO;
import com.greentrade.greentrade.services.TransactionService;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private TransactionDTO testTransactie;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testTransactie = new TransactionDTO(
            1L,
            1L, // koperId
            1L, // productId
            new BigDecimal("299.99"),
            LocalDateTime.now(),
            "IN_BEHANDELING"
        );
    }

    @Test
    @WithMockUser
    void whenGetAlleTransacties_thenSuccess() throws Exception {
        when(transactionService.getAlleTransacties())
            .thenReturn(Arrays.asList(testTransactie));

        mockMvc.perform(get("/api/transacties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("IN_BEHANDELING"))
                .andExpect(jsonPath("$[0].bedrag").value(299.99));
    }

    @Test
    @WithMockUser(roles = "KOPER")
    void whenMaakTransactie_thenSuccess() throws Exception {
        when(transactionService.maakTransactie(any(TransactionDTO.class)))
            .thenReturn(testTransactie);

        mockMvc.perform(post("/api/transacties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTransactie)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_BEHANDELING"));
    }

    @Test
    @WithMockUser
    void whenUpdateTransactieStatus_thenSuccess() throws Exception {
        TransactionDTO updatedTransactie = new TransactionDTO(
            1L, 1L, 1L, new BigDecimal("299.99"),
            LocalDateTime.now(), "VOLTOOID"
        );

        when(transactionService.updateTransactieStatus(anyLong(), any()))
            .thenReturn(updatedTransactie);

        mockMvc.perform(put("/api/transacties/{id}/status", 1L)
                .param("nieuweStatus", "VOLTOOID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VOLTOOID"));
    }

    @Test
    @WithMockUser(roles = "KOPER") 
    void whenGetTransactiesDoorKoper_thenSuccess() throws Exception {
        when(transactionService.getTransactiesDoorKoper(anyLong()))
            .thenReturn(Arrays.asList(testTransactie));
    
        mockMvc.perform(get("/api/transacties/koper/{koperId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].koperId").value(1));
    }

    @Test
    @WithMockUser
    void whenGetTransactiesDoorVerkoper_thenSuccess() throws Exception {
        when(transactionService.getTransactiesDoorVerkoper(anyLong()))
            .thenReturn(Arrays.asList(testTransactie));

        mockMvc.perform(get("/api/transacties/verkoper/{verkoperId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].koperId").value(1));
    }

    @Test
    @WithMockUser
    void whenGetTransactiesTussenData_thenSuccess() throws Exception {
        when(transactionService.getTransactiesTussenData(any(), any()))
            .thenReturn(Arrays.asList(testTransactie));

        mockMvc.perform(get("/api/transacties/periode")
                .param("start", LocalDateTime.now().minusDays(7).toString())
                .param("eind", LocalDateTime.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("IN_BEHANDELING"));
    }

    @Test
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/transacties"))
                .andExpect(status().isForbidden());
    }
}