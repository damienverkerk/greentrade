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

import com.greentrade.greentrade.dto.ReviewDTO;
import com.greentrade.greentrade.services.BeoordelingService;

@RestController
@RequestMapping("/api/beoordelingen")
public class BeoordelingController {

    private final BeoordelingService beoordelingService;

    @Autowired
    public BeoordelingController(BeoordelingService beoordelingService) {
        this.beoordelingService = beoordelingService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAlleBeoordelingen() {
        return new ResponseEntity<>(beoordelingService.getAlleBeoordelingen(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getBeoordelingById(@PathVariable Long id) {
        ReviewDTO review = beoordelingService.getBeoordelingById(id);
        return review != null ? ResponseEntity.ok(review) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> maakBeoordeling(@RequestBody ReviewDTO reviewDTO) {
        ReviewDTO nieuweBeoordeling = beoordelingService.maakBeoordeling(reviewDTO);
        return new ResponseEntity<>(nieuweBeoordeling, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateBeoordeling(@PathVariable Long id, @RequestBody ReviewDTO reviewDTO) {
        try {
            ReviewDTO updatedBeoordeling = beoordelingService.updateBeoordeling(id, reviewDTO);
            return ResponseEntity.ok(updatedBeoordeling);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderBeoordeling(@PathVariable Long id) {
        try {
            beoordelingService.verwijderBeoordeling(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getBeoordelingenVoorProduct(@PathVariable Long productId) {
        try {
            List<ReviewDTO> beoordelingen = beoordelingService.getBeoordelingenVoorProduct(productId);
            return ResponseEntity.ok(beoordelingen);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/gemiddelde-score/product/{productId}")
    public ResponseEntity<Double> getGemiddeldeScoreVoorProduct(@PathVariable Long productId) {
        try {
            Double gemiddeldeScore = beoordelingService.getGemiddeldeScoreVoorProduct(productId);
            return ResponseEntity.ok(gemiddeldeScore);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}