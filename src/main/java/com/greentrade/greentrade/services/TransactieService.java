package com.greentrade.greentrade.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.models.Transaction;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.TransactionRepository;

@Service
public class TransactieService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactieService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getAlleTransacties() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactieById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getTransactiesDoorKoper(User koper) {
        return transactionRepository.findByKoper(koper);
    }

    public List<Transaction> getTransactiesDoorVerkoper(User verkoper) {
        return transactionRepository.findByProduct_Verkoper(verkoper);
    }

    public List<Transaction> getTransactiesTussenData(LocalDateTime start, LocalDateTime eind) {
        return transactionRepository.findByDatumBetween(start, eind);
    }

    public Transaction maakTransactie(Transaction transactie) {
        return transactionRepository.save(transactie);
    }

    public Transaction updateTransactieStatus(Long id, String nieuweStatus) {
        Transaction transactie = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transactie niet gevonden met id: " + id));

        transactie.setStatus(nieuweStatus);
        return transactionRepository.save(transactie);
    }

    public void verwijderTransactie(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new RuntimeException("Transactie niet gevonden met id: " + id);
        }
        transactionRepository.deleteById(id);
    }

    public long getTotaalAantalTransacties() {
        return transactionRepository.count();
    }
}