package com.greentrade.greentrade.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.ProductVerification;
import com.greentrade.greentrade.models.VerificationStatus;

@Repository
public interface ProductVerificationRepository extends JpaRepository<ProductVerification, Long> {
    List<ProductVerification> findByStatus(VerificationStatus status);
    Optional<ProductVerification> findFirstByProductOrderBySubmissionDateDesc(Product product);
    List<ProductVerification> findByProductId(Long productId);
    List<ProductVerification> findByReviewerId(Long reviewerId);
}