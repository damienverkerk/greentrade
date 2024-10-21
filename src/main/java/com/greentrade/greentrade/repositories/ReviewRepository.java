package com.greentrade.greentrade.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.Review;
import com.greentrade.greentrade.models.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
    List<Review> findByRecensent(User recensent);
    Double averageScoreByProduct(Product product);
}