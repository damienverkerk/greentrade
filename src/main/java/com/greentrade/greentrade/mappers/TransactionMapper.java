package com.greentrade.greentrade.mappers;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.TransactionDTO;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.Transaction;
import com.greentrade.greentrade.models.User;

@Component
public class TransactionMapper {
    
    public TransactionDTO toDTO(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return new TransactionDTO(
                transaction.getId(),
                transaction.getBuyer().getId(),
                transaction.getProduct().getId(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getStatus()
        );
    }
    
    public Transaction toEntity(TransactionDTO dto, User buyer, Product product) {
        if (dto == null) {
            return null;
        }
        
        Transaction transaction = new Transaction();
        transaction.setId(dto.getId());
        transaction.setAmount(dto.getAmount());
        transaction.setDate(dto.getDate());
        transaction.setStatus(dto.getStatus());
        transaction.setBuyer(buyer);
        transaction.setProduct(product);
        
        return transaction;
    }
}