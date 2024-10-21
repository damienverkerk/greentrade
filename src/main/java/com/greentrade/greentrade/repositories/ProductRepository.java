package com.greentrade.greentrade.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.User;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByVerkoper(User verkoper);
    
    List<Product> findByNaamContainingIgnoreCase(String naam);
    
    List<Product> findByDuurzaamheidsScoreGreaterThanEqual(Integer score);
    
}