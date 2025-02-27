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
import com.greentrade.greentrade.services.ReviewService;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @Test
    @WithMockUser
    void whenGetAllReviews_thenSuccess() throws Exception {
        ReviewDTO review = new ReviewDTO(1L, 1L, 1L, 5, "Good product", LocalDateTime.now());
        
        when(reviewService.getAllReviews())
            .thenReturn(Arrays.asList(review));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(5));
    }

    @Test
    @WithMockUser
    void whenCreateReview_thenSuccess() throws Exception {
        ReviewDTO reviewDTO = new ReviewDTO(null, 1L, 1L, 4, "Nice product", LocalDateTime.now());
        ReviewDTO savedReview = new ReviewDTO(1L, 1L, 1L, 4, "Nice product", LocalDateTime.now());

        when(reviewService.createReview(any(ReviewDTO.class)))
            .thenReturn(savedReview);

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void whenGetReviewsForProduct_thenSuccess() throws Exception {
        ReviewDTO review = new ReviewDTO(1L, 1L, 1L, 5, "Good product", LocalDateTime.now());
        
        when(reviewService.getReviewsForProduct(anyLong()))
            .thenReturn(Arrays.asList(review));

        mockMvc.perform(get("/api/reviews/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(5));
    }

    @Test
    @WithMockUser
    void whenUpdateReview_thenSuccess() throws Exception {
        ReviewDTO reviewDTO = new ReviewDTO(1L, 1L, 1L, 3, "Update: Average product", LocalDateTime.now());

        when(reviewService.updateReview(anyLong(), any(ReviewDTO.class)))
            .thenReturn(reviewDTO);

        mockMvc.perform(put("/api/reviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(3));
    }

    @Test 
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isForbidden());
    }
}