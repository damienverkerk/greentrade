package com.greentrade.greentrade.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.TransactionDTO;
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

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public List<TransactionDTO> getAlleTransacties() {
        return transactionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransactieById(Long id) {
        return transactionRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<TransactionDTO> getTransactiesDoorKoper(Long koperId) {
        User koper = userRepository.findById(koperId)
                .orElseThrow(() -> new RuntimeException("Koper niet gevonden met id: " + koperId));
        return transactionRepository.findByKoper(koper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactiesDoorVerkoper(Long verkoperId) {
        User verkoper = userRepository.findById(verkoperId)
                .orElseThrow(() -> new RuntimeException("Verkoper niet gevonden met id: " + verkoperId));
        return transactionRepository.findByProduct_Verkoper(verkoper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactiesTussenData(LocalDateTime start, LocalDateTime eind) {
        return transactionRepository.findByDatumBetween(start, eind).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO maakTransactie(TransactionDTO transactionDTO) {
        Transaction transaction = convertToEntity(transactionDTO);
        Transaction savedTransaction = transactionRepository.save(transaction);
        return convertToDTO(savedTransaction);
    }

    public TransactionDTO updateTransactieStatus(Long id, String nieuweStatus) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transactie niet gevonden met id: " + id));
        transaction.setStatus(nieuweStatus);
        return convertToDTO(transactionRepository.save(transaction));
    }

    public void verwijderTransactie(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new RuntimeException("Transactie niet gevonden met id: " + id);
        }
        transactionRepository.deleteById(id);
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getKoper().getId(),
                transaction.getProduct().getId(),
                transaction.getBedrag(),
                transaction.getDatum(),
                transaction.getStatus()
        );
    }

    private Transaction convertToEntity(TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();
        transaction.setId(transactionDTO.getId());
        transaction.setBedrag(transactionDTO.getBedrag());
        transaction.setDatum(transactionDTO.getDatum());
        transaction.setStatus(transactionDTO.getStatus());

        User koper = userRepository.findById(transactionDTO.getKoperId())
                .orElseThrow(() -> new RuntimeException("Koper niet gevonden met id: " + transactionDTO.getKoperId()));
        transaction.setKoper(koper);

        Product product = productRepository.findById(transactionDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product niet gevonden met id: " + transactionDTO.getProductId()));
        transaction.setProduct(product);

        return transaction;
    }
}