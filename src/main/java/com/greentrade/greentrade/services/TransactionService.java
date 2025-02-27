package com.greentrade.greentrade.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.transaction.TransactionCreateRequest;
import com.greentrade.greentrade.dto.transaction.TransactionResponse;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
import com.greentrade.greentrade.mappers.TransactionMapper;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.Transaction;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.ProductRepository;
import com.greentrade.greentrade.repositories.TransactionRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final TransactionMapper transactionMapper;

    @Autowired
    public TransactionService(
            TransactionRepository transactionRepository, 
            UserRepository userRepository, 
            ProductRepository productRepository,
            TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.transactionMapper = transactionMapper;
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    public List<TransactionResponse> getTransactionsByBuyer(Long buyerId) {
        User buyer = findUserById(buyerId);
        return transactionRepository.findByBuyer(buyer).stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getTransactionsBySeller(Long sellerId) {
        User seller = findUserById(sellerId);
        return transactionRepository.findByProduct_Seller(seller).stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getTransactionsBetweenDates(LocalDateTime start, LocalDateTime end) {
        validateDateRange(start, end);
        return transactionRepository.findByDateBetween(start, end).stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse createTransaction(TransactionCreateRequest request) {
        validateTransactionAmount(request);

        try {
            User buyer = findUserById(request.getBuyerId());
            Product product = findProductById(request.getProductId());
            
            Transaction transaction = transactionMapper.createRequestToEntity(request, buyer, product);
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            return transactionMapper.toResponse(savedTransaction);
        } catch (IllegalArgumentException e) {
            throw e;  // Re-throw validation errors
        } catch (Exception e) {
            throw new RuntimeException("Failed to create transaction: " + e.getMessage());
        }
    }

    public TransactionResponse updateTransactionStatus(Long id, String newStatus) {
        validateTransactionStatus(newStatus);
        
        Transaction transaction = findTransactionById(id);
        transaction.setStatus(newStatus);
        
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
    }
    
    private void validateTransactionAmount(TransactionCreateRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
    }
    
    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
    
    private void validateTransactionStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction status cannot be empty");
        }
        
        // Check if status is valid
        List<String> validStatuses = List.of("PENDING", "PROCESSING", "COMPLETED", "CANCELLED", "REFUNDED");
        if (!validStatuses.contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid transaction status: " + status);
        }
    }
    
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
    
    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
    
    private Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }
}