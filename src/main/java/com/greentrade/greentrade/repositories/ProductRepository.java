package com.greentrade.greentrade.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.User;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findBySeller(User seller);
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    List<Product> findBySustainabilityScoreGreaterThanEqual(Integer score);
    
}