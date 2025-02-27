package com.greentrade.greentrade.mappers;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.ReviewDTO;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.Review;
import com.greentrade.greentrade.models.User;

@Component
public class ReviewMapper {
    
    public ReviewDTO toDTO(Review review) {
        if (review == null) {
            return null;
        }
        
        return new ReviewDTO(
                review.getId(),
                review.getProduct().getId(),
                review.getReviewer().getId(),
                review.getScore(),
                review.getComment(),
                review.getDate()
        );
    }
    
    public Review toEntity(ReviewDTO dto, Product product, User reviewer) {
        if (dto == null) {
            return null;
        }
        
        Review review = new Review();
        review.setId(dto.getId());
        review.setScore(dto.getScore());
        review.setComment(dto.getComment());
        review.setDate(dto.getDate());
        review.setProduct(product);
        review.setReviewer(reviewer);
        
        return review;
    }
}