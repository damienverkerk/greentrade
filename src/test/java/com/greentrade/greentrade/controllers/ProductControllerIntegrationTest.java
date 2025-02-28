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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.product.ProductCreateRequest;
import com.greentrade.greentrade.dto.product.ProductResponse;
import com.greentrade.greentrade.dto.product.ProductUpdateRequest;
import com.greentrade.greentrade.services.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse testProduct;
    private ProductCreateRequest createRequest;
    private ProductUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        
        testProduct = ProductResponse.builder()
            .id(1L) 
            .name("Sustainable Chair")
            .description("Ergonomic office chair made from recycled materials")
            .price(new BigDecimal("299.99"))
            .sustainabilityScore(85)
            .sustainabilityCertificate("ISO14001")
            .sellerId(1L)
            .build();
            
        
        createRequest = ProductCreateRequest.builder()
            .name("Sustainable Chair")
            .description("Ergonomic office chair made from recycled materials")
            .price(new BigDecimal("299.99"))
            .sustainabilityScore(85)
            .sustainabilityCertificate("ISO14001")
            .sellerId(1L)
            .build();
            
        updateRequest = ProductUpdateRequest.builder()
            .name("Updated Chair")
            .description("Updated description")
            .price(new BigDecimal("349.99"))
            .sustainabilityScore(90)
            .sustainabilityCertificate("ISO14001")
            .sellerId(1L)
            .build();
    }

    @Test
    @WithMockUser
    void whenGetAllProducts_thenSuccess() throws Exception {
        
        when(productService.getAllProducts())
            .thenReturn(Arrays.asList(testProduct));

        
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sustainable Chair"))
                .andExpect(jsonPath("$[0].sustainabilityScore").value(85));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void whenCreateProduct_thenSuccess() throws Exception {
        
        when(productService.createProduct(any(ProductCreateRequest.class)))
            .thenReturn(testProduct);

        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sustainable Chair"));
    }

    @Test
    @WithMockUser
    void whenSearchProducts_thenSuccess() throws Exception {
        
        when(productService.searchProductsByName(anyString()))
            .thenReturn(Arrays.asList(testProduct));

        
        mockMvc.perform(get("/api/products/search")
                .param("name", "chair"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sustainable Chair"));
    }

    @Test
    @WithMockUser
    void whenGetSustainableProducts_thenSuccess() throws Exception {
        
        when(productService.getProductsBySustainabilityScore(any()))
            .thenReturn(Arrays.asList(testProduct));

        
        mockMvc.perform(get("/api/products/sustainable")
                .param("minimumScore", "80"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sustainabilityScore").value(85));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void whenUpdateProduct_thenSuccess() throws Exception {
        
        ProductResponse updatedProduct = ProductResponse.builder()
            .id(1L)
            .name("Updated Chair")
            .description("Updated description")
            .price(new BigDecimal("349.99"))
            .sustainabilityScore(90)
            .sustainabilityCertificate("ISO14001")
            .sellerId(1L)
            .build();
            
        when(productService.updateProduct(anyLong(), any(ProductUpdateRequest.class)))
            .thenReturn(updatedProduct);

        
        mockMvc.perform(put("/api/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Chair"))
                .andExpect(jsonPath("$.price").value(349.99));
    }
    
    @Test
    @WithMockUser
    void whenGetProductById_thenSuccess() throws Exception {
        
        when(productService.getProductById(1L))
            .thenReturn(testProduct);
            
        
        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sustainable Chair"))
                .andExpect(jsonPath("$.sustainabilityScore").value(85));
    }

    @Test
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());
    }
}