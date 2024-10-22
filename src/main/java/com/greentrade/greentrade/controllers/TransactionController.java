package com.greentrade.greentrade.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greentrade.greentrade.dto.TransactionDTO;
import com.greentrade.greentrade.services.TransactionService;

@RestController
@RequestMapping("/api/transacties")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAlleTransacties() {
        return new ResponseEntity<>(transactionService.getAlleTransacties(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactieById(@PathVariable Long id) {
        TransactionDTO transactie = transactionService.getTransactieById(id);
        return transactie != null ? new ResponseEntity<>(transactie, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> maakTransactie(@RequestBody TransactionDTO transactieDTO) {
        TransactionDTO nieuweTransactie = transactionService.maakTransactie(transactieDTO);
        return new ResponseEntity<>(nieuweTransactie, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TransactionDTO> updateTransactieStatus(@PathVariable Long id, @RequestParam String nieuweStatus) {
        try {
            TransactionDTO updatedTransactie = transactionService.updateTransactieStatus(id, nieuweStatus);
            return new ResponseEntity<>(updatedTransactie, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderTransactie(@PathVariable Long id) {
        try {
            transactionService.verwijderTransactie(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/koper/{koperId}")
    public ResponseEntity<List<TransactionDTO>> getTransactiesDoorKoper(@PathVariable Long koperId) {
        List<TransactionDTO> transacties = transactionService.getTransactiesDoorKoper(koperId);
        return new ResponseEntity<>(transacties, HttpStatus.OK);
    }

    @GetMapping("/verkoper/{verkoperId}")
    public ResponseEntity<List<TransactionDTO>> getTransactiesDoorVerkoper(@PathVariable Long verkoperId) {
        List<TransactionDTO> transacties = transactionService.getTransactiesDoorVerkoper(verkoperId);
        return new ResponseEntity<>(transacties, HttpStatus.OK);
    }

    @GetMapping("/periode")
    public ResponseEntity<List<TransactionDTO>> getTransactiesTussenData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime eind) {
        List<TransactionDTO> transacties = transactionService.getTransactiesTussenData(start, eind);
        return new ResponseEntity<>(transacties, HttpStatus.OK);
    }
}