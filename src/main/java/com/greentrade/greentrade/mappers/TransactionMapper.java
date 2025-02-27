package com.greentrade.greentrade.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.transaction.TransactionCreateRequest;
import com.greentrade.greentrade.dto.transaction.TransactionResponse;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.Transaction;
import com.greentrade.greentrade.models.User;

@Component
public class TransactionMapper {
    
    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return TransactionResponse.builder()
                .id(transaction.getId())
                .buyerId(transaction.getBuyer().getId())
                .productId(transaction.getProduct().getId())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .status(transaction.getStatus())
                .build();
    }
    
    public Transaction createRequestToEntity(TransactionCreateRequest request, User buyer, Product product) {
        if (request == null) {
            return null;
        }
        
        Transaction transaction = new Transaction();
        transaction.setBuyer(buyer);
        transaction.setProduct(product);
        transaction.setAmount(request.getAmount());
        transaction.setDate(LocalDateTime.now());
        transaction.setStatus("PENDING");
        
        return transaction;
    }
}