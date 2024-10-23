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

import com.greentrade.greentrade.dto.MessageDTO;
import com.greentrade.greentrade.services.BerichtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/berichten")
@Tag(name = "Berichten", description = "API endpoints voor het beheren van berichten tussen gebruikers")
public class BerichtController {

    private final BerichtService berichtService;

    @Autowired
    public BerichtController(BerichtService berichtService) {
        this.berichtService = berichtService;
    }

    @Operation(summary = "Haal alle berichten op")
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getAlleBerichten() {
        return ResponseEntity.ok(berichtService.getAlleBerichten());
    }

    @Operation(summary = "Haal een specifiek bericht op")
    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> getBerichtById(@PathVariable Long id) {
        return berichtService.getBerichtById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Verstuur een nieuw bericht")
    @PostMapping
    public ResponseEntity<MessageDTO> verstuurBericht(@Valid @RequestBody MessageDTO bericht) {
        MessageDTO nieuwBericht = berichtService.verstuurBericht(bericht);
        return new ResponseEntity<>(nieuwBericht, HttpStatus.CREATED);
    }

    @Operation(summary = "Markeer een bericht als gelezen")
    @PutMapping("/{id}/markeer-gelezen")
    public ResponseEntity<MessageDTO> markeerAlsGelezen(@PathVariable Long id) {
        try {
            MessageDTO gelezenBericht = berichtService.markeerAlsGelezen(id);
            return ResponseEntity.ok(gelezenBericht);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Verwijder een bericht")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderBericht(@PathVariable Long id) {
        try {
            berichtService.verwijderBericht(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Haal ontvangen berichten op voor een gebruiker")
    @GetMapping("/ontvangen/{gebruikerId}")
    public ResponseEntity<List<MessageDTO>> getOntvangenBerichtenVoorGebruiker(@PathVariable Long gebruikerId) {
        try {
            List<MessageDTO> berichten = berichtService.getOntvangenBerichtenVoorGebruiker(gebruikerId);
            return ResponseEntity.ok(berichten);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Haal verzonden berichten op van een gebruiker")
    @GetMapping("/verzonden/{gebruikerId}")
    public ResponseEntity<List<MessageDTO>> getVerzondenBerichtenVanGebruiker(@PathVariable Long gebruikerId) {
        try {
            List<MessageDTO> berichten = berichtService.getVerzondenBerichtenVanGebruiker(gebruikerId);
            return ResponseEntity.ok(berichten);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Haal ongelezen berichten op voor een gebruiker")
    @GetMapping("/ongelezen/{gebruikerId}")
    public ResponseEntity<List<MessageDTO>> getOngelezenBerichtenVoorGebruiker(@PathVariable Long gebruikerId) {
        try {
            List<MessageDTO> berichten = berichtService.getOngelezenBerichtenVoorGebruiker(gebruikerId);
            return ResponseEntity.ok(berichten);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}