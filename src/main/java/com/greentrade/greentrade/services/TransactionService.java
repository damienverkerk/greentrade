package com.greentrade.greentrade.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.TransactionDTO;
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

    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    public List<TransactionDTO> getTransactionsByBuyer(Long buyerId) {
        User buyer = findUserById(buyerId);
        return transactionRepository.findByBuyer(buyer).stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactionsBySeller(Long sellerId) {
        User seller = findUserById(sellerId);
        return transactionRepository.findByProduct_Seller(seller).stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactionsBetweenDates(LocalDateTime start, LocalDateTime end) {
        validateDateRange(start, end);
        return transactionRepository.findByDateBetween(start, end).stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
        validateTransactionAmount(transactionDTO);

        try {
            User buyer = findUserById(transactionDTO.getBuyerId());
            Product product = findProductById(transactionDTO.getProductId());
            
            Transaction transaction = transactionMapper.toEntity(transactionDTO, buyer, product);
            
            if (transaction.getDate() == null) {
                transaction.setDate(LocalDateTime.now());
            }
            
            Transaction savedTransaction = transactionRepository.save(transaction);
            return transactionMapper.toDTO(savedTransaction);
        } catch (IllegalArgumentException e) {
            throw e;  // Re-throw validation errors
        } catch (Exception e) {
            throw new RuntimeException("Failed to create transaction: " + e.getMessage());
        }
    }

    public TransactionDTO updateTransactionStatus(Long id, String newStatus) {
        validateTransactionStatus(newStatus);
        
        Transaction transaction = findTransactionById(id);
        transaction.setStatus(newStatus);
        
        return transactionMapper.toDTO(transactionRepository.save(transaction));
    }

    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
    }
    
    private void validateTransactionAmount(TransactionDTO transactionDTO) {
        if (transactionDTO.getAmount() == null || transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
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
        
        // Hier zou je kunnen controleren of de status een geldige waarde is
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