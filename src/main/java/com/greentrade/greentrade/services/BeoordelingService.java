package com.greentrade.greentrade.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.Review;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.ReviewRepository;

@Service
public class BeoordelingService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public BeoordelingService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<Review> getAlleBeoordelingen() {
        return reviewRepository.findAll();
    }

    public Optional<Review> getBeoordelingById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<Review> getBeoordelingenVoorProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }

    public List<Review> getBeoordelingenDoorRecensent(User recensent) {
        return reviewRepository.findByRecensent(recensent);
    }

    public Double getGemiddeldeScoreVoorProduct(Product product) {
        return reviewRepository.averageScoreByProduct(product);
    }

    public Review maakBeoordeling(Review beoordeling) {
        return reviewRepository.save(beoordeling);
    }

    public Review updateBeoordeling(Long id, Review beoordelingDetails) {
        Review beoordeling = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Beoordeling niet gevonden met id: " + id));

        beoordeling.setScore(beoordelingDetails.getScore());
        beoordeling.setCommentaar(beoordelingDetails.getCommentaar());

        return reviewRepository.save(beoordeling);
    }

    public void verwijderBeoordeling(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Beoordeling niet gevonden met id: " + id);
        }
        reviewRepository.deleteById(id);
    }
}