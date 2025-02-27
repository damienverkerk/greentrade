package com.greentrade.greentrade.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.review.ReviewCreateRequest;
import com.greentrade.greentrade.dto.review.ReviewResponse;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.Review;
import com.greentrade.greentrade.models.User;

@Component
public class ReviewMapper {
    
    public ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .reviewerId(review.getReviewer().getId())
                .score(review.getScore())
                .comment(review.getComment())
                .date(review.getDate())
                .build();
    }
    
    public Review createRequestToEntity(ReviewCreateRequest request, Product product, User reviewer) {
        if (request == null) {
            return null;
        }
        
        Review review = new Review();
        review.setProduct(product);
        review.setReviewer(reviewer);
        review.setScore(request.getScore());
        review.setComment(request.getComment());
        review.setDate(LocalDateTime.now());
        
        return review;
    }
}