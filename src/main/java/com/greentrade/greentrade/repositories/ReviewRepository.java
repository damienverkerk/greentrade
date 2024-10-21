package com.greentrade.greentrade.repositories;

import com.greentrade.greentrade.models.Review;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.User; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
    List<Review> findByRecensent(User recensent);
    
    @Query("SELECT AVG(r.score) FROM Review r WHERE r.product = :product")
    Double averageScoreByProduct(Product product);
}