package com.greentrade.greentrade.dto;

import java.time.LocalDateTime;

public class ReviewDTO {
    private Long id;
    private Long productId;
    private Long recensentId;
    private int score;
    private String commentaar;
    private LocalDateTime datum;

    // Constructors
    public ReviewDTO() {}

    public ReviewDTO(Long id, Long productId, Long recensentId, int score, String commentaar, LocalDateTime datum) {
        this.id = id;
        this.productId = productId;
        this.recensentId = recensentId;
        this.score = score;
        this.commentaar = commentaar;
        this.datum = datum;
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

    public Long getRecensentId() {
        return recensentId;
    }

    public void setRecensentId(Long recensentId) {
        this.recensentId = recensentId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getCommentaar() {
        return commentaar;
    }

    public void setCommentaar(String commentaar) {
        this.commentaar = commentaar;
    }

    public LocalDateTime getDatum() {
        return datum;
    }

    public void setDatum(LocalDateTime datum) {
        this.datum = datum;
    }
}