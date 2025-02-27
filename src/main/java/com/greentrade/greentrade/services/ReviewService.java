package com.greentrade.greentrade.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.review.ReviewCreateRequest;
import com.greentrade.greentrade.dto.review.ReviewResponse;
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

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ReviewResponse getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(reviewMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }

    public List<ReviewResponse> getReviewsForProduct(Long productId) {
        Product product = findProductById(productId);
        return reviewRepository.findByProduct(product).stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Double getAverageScoreForProduct(Long productId) {
        Product product = findProductById(productId);
        return reviewRepository.averageScoreByProduct(product);
    }

    public ReviewResponse createReview(ReviewCreateRequest request) {
        validateReviewData(request);
        
        Product product = findProductById(request.getProductId());
        User reviewer = findUserById(request.getReviewerId());
        
        Review review = reviewMapper.createRequestToEntity(request, product, reviewer);
        Review savedReview = reviewRepository.save(review);
        
        return reviewMapper.toResponse(savedReview);
    }

    public ReviewResponse updateReview(Long id, ReviewCreateRequest request) {
        validateReviewData(request);
        
        Review review = findReviewById(id);
        
        Product product = findProductById(request.getProductId());
        User reviewer = findUserById(request.getReviewerId());
        
        review.setProduct(product);
        review.setReviewer(reviewer);
        review.setScore(request.getScore());
        review.setComment(request.getComment());
        
        Review updatedReview = reviewRepository.save(review);
        
        return reviewMapper.toResponse(updatedReview);
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
    
    private void validateReviewData(ReviewCreateRequest request) {
        if (request.getScore() < 1 || request.getScore() > 5) {
            throw new IllegalArgumentException("Review score must be between 1 and 5");
        }
    }
}