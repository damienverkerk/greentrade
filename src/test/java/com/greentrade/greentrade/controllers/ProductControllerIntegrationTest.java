package com.greentrade.greentrade.controllers;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.greentrade.greentrade.dto.ProductDTO;
import com.greentrade.greentrade.services.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductDTO testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new ProductDTO(
            1L, 
            "Duurzame Stoel",
            "Ergonomische bureaustoel van gerecycled materiaal",
            new BigDecimal("299.99"),
            85,
            "ISO14001",
            1L
        );
    }

    @Test
    @WithMockUser
    void whenGetAllProducts_thenSuccess() throws Exception {
        when(productService.getAllProducts())
            .thenReturn(Arrays.asList(testProduct));

        mockMvc.perform(get("/api/producten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].naam").value("Duurzame Stoel"))
                .andExpect(jsonPath("$[0].duurzaamheidsScore").value(85));
    }

    @Test
    @WithMockUser(roles = "VERKOPER")
    void whenCreateProduct_thenSuccess() throws Exception {
        when(productService.createProduct(any(ProductDTO.class)))
            .thenReturn(testProduct);

        mockMvc.perform(post("/api/producten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.naam").value("Duurzame Stoel"));
    }

    @Test
    @WithMockUser
    void whenSearchProducts_thenSuccess() throws Exception {
        when(productService.searchProductsByName(anyString()))
            .thenReturn(Arrays.asList(testProduct));

        mockMvc.perform(get("/api/producten/zoek")
                .param("naam", "stoel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].naam").value("Duurzame Stoel"));
    }

    @Test
    @WithMockUser
    void whenGetDuurzameProducten_thenSuccess() throws Exception {
        when(productService.getProductsByDuurzaamheidsScore(any()))
            .thenReturn(Arrays.asList(testProduct));

        mockMvc.perform(get("/api/producten/duurzaam")
                .param("minimumScore", "80"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].duurzaamheidsScore").value(85));
    }

    @Test
    @WithMockUser(roles = "VERKOPER")
    void whenUpdateProduct_thenSuccess() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductDTO.class)))
            .thenReturn(testProduct);

        mockMvc.perform(put("/api/producten/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value("Duurzame Stoel"));
    }

    @Test
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/producten"))
                .andExpect(status().isForbidden());
    }
}