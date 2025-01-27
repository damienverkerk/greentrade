package com.greentrade.greentrade.controllers;

import java.time.LocalDateTime;
import java.util.Arrays;

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
import com.greentrade.greentrade.dto.ReviewDTO;
import com.greentrade.greentrade.services.BeoordelingService;

@SpringBootTest
@AutoConfigureMockMvc
class BeoordelingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BeoordelingService beoordelingService;

    @Test
    @WithMockUser
    void whenGetAllBeoordelingen_thenSuccess() throws Exception {
        ReviewDTO review = new ReviewDTO(1L, 1L, 1L, 5, "Goed product", LocalDateTime.now());
        
        when(beoordelingService.getAlleBeoordelingen())
            .thenReturn(Arrays.asList(review));

        mockMvc.perform(get("/api/beoordelingen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(5));
    }

    @Test
    @WithMockUser
    void whenCreateBeoordeling_thenSuccess() throws Exception {
        ReviewDTO reviewDTO = new ReviewDTO(null, 1L, 1L, 4, "Prima product", LocalDateTime.now());
        ReviewDTO savedReview = new ReviewDTO(1L, 1L, 1L, 4, "Prima product", LocalDateTime.now());

        when(beoordelingService.maakBeoordeling(any(ReviewDTO.class)))
            .thenReturn(savedReview);

        mockMvc.perform(post("/api/beoordelingen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void whenGetBeoordelingenVoorProduct_thenSuccess() throws Exception {
        ReviewDTO review = new ReviewDTO(1L, 1L, 1L, 5, "Goed product", LocalDateTime.now());
        
        when(beoordelingService.getBeoordelingenVoorProduct(anyLong()))
            .thenReturn(Arrays.asList(review));

        mockMvc.perform(get("/api/beoordelingen/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(5));
    }

    @Test
    @WithMockUser
    void whenUpdateBeoordeling_thenSuccess() throws Exception {
        ReviewDTO reviewDTO = new ReviewDTO(1L, 1L, 1L, 3, "Update: Matig product", LocalDateTime.now());

        when(beoordelingService.updateBeoordeling(anyLong(), any(ReviewDTO.class)))
            .thenReturn(reviewDTO);

        mockMvc.perform(put("/api/beoordelingen/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(3));
    }

    @Test 
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/beoordelingen"))
                .andExpect(status().isForbidden());
    }
}