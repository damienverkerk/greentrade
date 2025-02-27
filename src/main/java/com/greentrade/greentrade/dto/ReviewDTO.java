package com.greentrade.greentrade.dto;

import java.time.LocalDateTime;

public class ReviewDTO {
    private Long id;
    private Long productId;
    private Long reviewerId;
    private int score;
    private String comment;
    private LocalDateTime date;

    // Constructors
    public ReviewDTO() {}

    public ReviewDTO(Long id, Long productId, Long reviewerId, int score, String comment, LocalDateTime date) {
        this.id = id;
        this.productId = productId;
        this.reviewerId = reviewerId;
        this.score = score;
        this.comment = comment;
        this.date = date;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}