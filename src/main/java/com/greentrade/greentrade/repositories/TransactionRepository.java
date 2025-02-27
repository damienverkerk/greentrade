package com.greentrade.greentrade.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greentrade.greentrade.models.Transaction;
import com.greentrade.greentrade.models.User;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyer(User buyer);
    List<Transaction> findByProduct_Seller(User seller);
    List<Transaction> findByDateBetween(LocalDateTime start, LocalDateTime end);
}