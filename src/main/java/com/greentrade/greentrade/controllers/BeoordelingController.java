package com.greentrade.greentrade.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.Review;
import com.greentrade.greentrade.services.BeoordelingService;
import com.greentrade.greentrade.services.ProductService;

@RestController
@RequestMapping("/api/beoordelingen")
public class BeoordelingController {

    private final BeoordelingService beoordelingService;
    private final ProductService productService;

    @Autowired
    public BeoordelingController(BeoordelingService beoordelingService, ProductService productService) {
        this.beoordelingService = beoordelingService;
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAlleBeoordelingen() {
        return new ResponseEntity<>(beoordelingService.getAlleBeoordelingen(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getBeoordelingById(@PathVariable Long id) {
        return beoordelingService.getBeoordelingById(id)
                .map(beoordeling -> new ResponseEntity<>(beoordeling, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Review> maakBeoordeling(@RequestBody Review beoordeling) {
        Review nieuweBeoordeling = beoordelingService.maakBeoordeling(beoordeling);
        return new ResponseEntity<>(nieuweBeoordeling, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateBeoordeling(@PathVariable Long id, @RequestBody Review beoordeling) {
        try {
            Review updatedBeoordeling = beoordelingService.updateBeoordeling(id, beoordeling);
            return new ResponseEntity<>(updatedBeoordeling, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderBeoordeling(@PathVariable Long id) {
        try {
            beoordelingService.verwijderBeoordeling(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getBeoordelingenVoorProduct(@PathVariable Long productId) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product niet gevonden met id: " + productId));
        List<Review> beoordelingen = beoordelingService.getBeoordelingenVoorProduct(product);
        return new ResponseEntity<>(beoordelingen, HttpStatus.OK);
    }

    @GetMapping("/gemiddelde-score/product/{productId}")
    public ResponseEntity<Double> getGemiddeldeScoreVoorProduct(@PathVariable Long productId) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product niet gevonden met id: " + productId));
        Double gemiddeldeScore = beoordelingService.getGemiddeldeScoreVoorProduct(product);
        return new ResponseEntity<>(gemiddeldeScore, HttpStatus.OK);
    }
}