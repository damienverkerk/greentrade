package com.greentrade.greentrade.controllers;

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
import com.greentrade.greentrade.dto.review.ReviewCreateRequest;
import com.greentrade.greentrade.dto.review.ReviewResponse;
import com.greentrade.greentrade.services.ReviewService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    private ReviewResponse testReview;
    private ReviewCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        
        testReview = ReviewResponse.builder()
                .id(1L)
                .productId(1L)
                .reviewerId(1L)
                .score(5)
                .comment("Good product")
                .date(LocalDateTime.now())
                .build();
        
        
        createRequest = ReviewCreateRequest.builder()
                .productId(1L)
                .reviewerId(1L)
                .score(5)
                .comment("Good product")
                .build();
    }

    @Test
    @WithMockUser
    void whenGetAllReviews_thenSuccess() throws Exception {
        
        when(reviewService.getAllReviews())
            .thenReturn(Arrays.asList(testReview));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(5));
    }

    @Test
    @WithMockUser
    void whenCreateReview_thenSuccess() throws Exception {
        
        when(reviewService.createReview(any(ReviewCreateRequest.class)))
            .thenReturn(testReview);

        
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void whenGetReviewsForProduct_thenSuccess() throws Exception {
        
        when(reviewService.getReviewsForProduct(anyLong()))
            .thenReturn(Arrays.asList(testReview));

        
        mockMvc.perform(get("/api/reviews/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(5));
    }

    @Test
    @WithMockUser
    void whenUpdateReview_thenSuccess() throws Exception {
        
        ReviewResponse updatedReview = ReviewResponse.builder()
                .id(1L)
                .productId(1L)
                .reviewerId(1L)
                .score(3)
                .comment("Update: Average product")
                .date(LocalDateTime.now())
                .build();
                
        when(reviewService.updateReview(anyLong(), any(ReviewCreateRequest.class)))
            .thenReturn(updatedReview);

        
        ReviewCreateRequest updateRequest = ReviewCreateRequest.builder()
                .productId(1L)
                .reviewerId(1L)
                .score(3)
                .comment("Update: Average product")
                .build();

        
        mockMvc.perform(put("/api/reviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(3));
    }
    
    @Test
    @WithMockUser
    void whenGetReviewById_thenSuccess() throws Exception {
        
        when(reviewService.getReviewById(1L))
            .thenReturn(testReview);

        
        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.score").value(5));
    }
    
    @Test
    @WithMockUser
    void whenGetAverageScoreForProduct_thenSuccess() throws Exception {
        
        when(reviewService.getAverageScoreForProduct(1L))
            .thenReturn(4.5);

        
        mockMvc.perform(get("/api/reviews/average-score/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.5));
    }

    @Test 
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isForbidden());
    }
}