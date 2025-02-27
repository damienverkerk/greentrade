package com.greentrade.greentrade.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greentrade.greentrade.dto.ReviewDTO;
import com.greentrade.greentrade.services.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "API endpoints for managing product reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(
        summary = "Get all reviews",
        description = "Retrieves a list of all reviews in the system"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Reviews successfully retrieved",
            content = @Content(schema = @Schema(implementation = ReviewDTO.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        return new ResponseEntity<>(reviewService.getAllReviews(), HttpStatus.OK);
    }

    @Operation(
        summary = "Get a specific review",
        description = "Retrieves a specific review based on its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review successfully found"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(
            @Parameter(description = "ID of the review", required = true)
            @PathVariable Long id) {
        ReviewDTO review = reviewService.getReviewById(id);
        return review != null ? ResponseEntity.ok(review) : ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Create a new review",
        description = "Creates a new review for a product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Review successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(
            @Parameter(description = "Review data", required = true)
            @Valid @RequestBody ReviewDTO reviewDTO) {
        ReviewDTO newReview = reviewService.createReview(reviewDTO);
        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Update an existing review",
        description = "Updates an existing review with new data"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review successfully updated"),
        @ApiResponse(responseCode = "404", description = "Review not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(
            @Parameter(description = "ID of the review", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated review data", required = true)
            @Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            ReviewDTO updatedReview = reviewService.updateReview(id, reviewDTO);
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Delete a review",
        description = "Deletes a review from the system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Review successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "ID of the review", required = true)
            @PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Get reviews for a product",
        description = "Retrieves all reviews for a specific product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reviews successfully retrieved"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsForProduct(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long productId) {
        try {
            List<ReviewDTO> reviews = reviewService.getReviewsForProduct(productId);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Get average score for a product",
        description = "Calculates and retrieves the average review score for a specific product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Average score successfully calculated"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/average-score/product/{productId}")
    public ResponseEntity<Double> getAverageScoreForProduct(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long productId) {
        try {
            Double averageScore = reviewService.getAverageScoreForProduct(productId);
            return ResponseEntity.ok(averageScore);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}