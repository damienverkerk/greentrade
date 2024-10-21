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

import com.greentrade.greentrade.models.Transaction;
import com.greentrade.greentrade.services.TransactieService;

@RestController
@RequestMapping("/api/transacties")
public class TransactieController {

    private final TransactieService transactieService;

    @Autowired
    public TransactieController(TransactieService transactieService) {
        this.transactieService = transactieService;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAlleTransacties() {
        return new ResponseEntity<>(transactieService.getAlleTransacties(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactieById(@PathVariable Long id) {
        return transactieService.getTransactieById(id)
                .map(transactie -> new ResponseEntity<>(transactie, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Transaction> maakTransactie(@RequestBody Transaction transactie) {
        Transaction nieuweTransactie = transactieService.maakTransactie(transactie);
        return new ResponseEntity<>(nieuweTransactie, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Transaction> updateTransactieStatus(@PathVariable Long id, @RequestParam String nieuweStatus) {
        try {
            Transaction updatedTransactie = transactieService.updateTransactieStatus(id, nieuweStatus);
            return new ResponseEntity<>(updatedTransactie, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderTransactie(@PathVariable Long id) {
        try {
            transactieService.verwijderTransactie(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/periode")
    public ResponseEntity<List<Transaction>> getTransactiesTussenData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime eind) {
        List<Transaction> transacties = transactieService.getTransactiesTussenData(start, eind);
        return new ResponseEntity<>(transacties, HttpStatus.OK);
    }
}
