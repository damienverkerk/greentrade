package com.greentrade.greentrade.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.ReviewDTO;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
import com.greentrade.greentrade.mappers.ReviewMapper;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.Review;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.ProductRepository;
import com.greentrade.greentrade.repositories.ReviewRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Autowired
    public ReviewService(
            ReviewRepository reviewRepository, 
            ProductRepository productRepository,
            UserRepository userRepository,
            ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.reviewMapper = reviewMapper;
    }

    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ReviewDTO getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(reviewMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }

    public List<ReviewDTO> getReviewsForProduct(Long productId) {
        Product product = findProductById(productId);
        return reviewRepository.findByProduct(product).stream()
                .map(reviewMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Double getAverageScoreForProduct(Long productId) {
        Product product = findProductById(productId);
        return reviewRepository.averageScoreByProduct(product);
    }

    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        validateReviewData(reviewDTO);
        
        Product product = findProductById(reviewDTO.getProductId());
        User reviewer = findUserById(reviewDTO.getReviewerId());
        
        Review review = reviewMapper.toEntity(reviewDTO, product, reviewer);
        Review savedReview = reviewRepository.save(review);
        
        return reviewMapper.toDTO(savedReview);
    }

    public ReviewDTO updateReview(Long id, ReviewDTO reviewDTO) {
        validateReviewData(reviewDTO);
        
        Review review = findReviewById(id);
        
        updateReviewFields(review, reviewDTO);
        Review updatedReview = reviewRepository.save(review);
        
        return reviewMapper.toDTO(updatedReview);
    }

    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }
    
    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
    
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
    
    private Review findReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }
    
    private void validateReviewData(ReviewDTO reviewDTO) {
        if (reviewDTO.getScore() < 1 || reviewDTO.getScore() > 5) {
            throw new IllegalArgumentException("Review score must be between 1 and 5");
        }
    }
    
    private void updateReviewFields(Review review, ReviewDTO reviewDTO) {
        review.setScore(reviewDTO.getScore());
        review.setComment(reviewDTO.getComment());
    }
}