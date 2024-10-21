package com.greentrade.greentrade.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "beoordelingen")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "recensent_id", nullable = false)
    private User recensent;

    @Column(nullable = false)
    private int score;

    @Column(length = 1000)
    private String commentaar;

    @Column(nullable = false)
    private LocalDateTime datum;

    // Constructors
    public Review() {}

    public Review(Product product, User recensent, int score, String commentaar, LocalDateTime datum) {
        this.product = product;
        this.recensent = recensent;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getRecensent() {
        return recensent;
    }

    public void setRecensent(User recensent) {
        this.recensent = recensent;
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