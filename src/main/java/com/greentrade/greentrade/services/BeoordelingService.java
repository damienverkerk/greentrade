package com.greentrade.greentrade.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.ReviewDTO;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.Review;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.ProductRepository;
import com.greentrade.greentrade.repositories.ReviewRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class BeoordelingService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public BeoordelingService(ReviewRepository reviewRepository, 
                            ProductRepository productRepository,
                            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<ReviewDTO> getAlleBeoordelingen() {
        return reviewRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ReviewDTO getBeoordelingById(Long id) {
        return reviewRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<ReviewDTO> getBeoordelingenVoorProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product niet gevonden met id: " + productId));
        return reviewRepository.findByProduct(product).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Double getGemiddeldeScoreVoorProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product niet gevonden met id: " + productId));
        return reviewRepository.averageScoreByProduct(product);
    }

    public ReviewDTO maakBeoordeling(ReviewDTO reviewDTO) {
        Review review = convertToEntity(reviewDTO);
        Review savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview);
    }

    public ReviewDTO updateBeoordeling(Long id, ReviewDTO reviewDTO) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Beoordeling niet gevonden met id: " + id));
        
        review.setScore(reviewDTO.getScore());
        review.setCommentaar(reviewDTO.getCommentaar());
        
        Review updatedReview = reviewRepository.save(review);
        return convertToDTO(updatedReview);
    }

    public void verwijderBeoordeling(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Beoordeling niet gevonden met id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    private ReviewDTO convertToDTO(Review review) {
        return new ReviewDTO(
            review.getId(),
            review.getProduct().getId(),
            review.getRecensent().getId(),
            review.getScore(),
            review.getCommentaar(),
            review.getDatum()
        );
    }

    private Review convertToEntity(ReviewDTO reviewDTO) {
        Review review = new Review();
        review.setScore(reviewDTO.getScore());
        review.setCommentaar(reviewDTO.getCommentaar());
        review.setDatum(reviewDTO.getDatum());

        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product niet gevonden met id: " + reviewDTO.getProductId()));
        review.setProduct(product);

        User recensent = userRepository.findById(reviewDTO.getRecensentId())
                .orElseThrow(() -> new RuntimeException("Recensent niet gevonden met id: " + reviewDTO.getRecensentId()));
        review.setRecensent(recensent);

        return review;
    }
}