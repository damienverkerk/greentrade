package com.greentrade.greentrade.controllers;

import com.greentrade.greentrade.models.Message;
import com.greentrade.greentrade.services.BerichtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/berichten")
public class BerichtController {

    private final BerichtService berichtService;

    @Autowired
    public BerichtController(BerichtService berichtService) {
        this.berichtService = berichtService;
    }

    @GetMapping
    public ResponseEntity<List<Message>> getAlleBerichten() {
        return new ResponseEntity<>(berichtService.getAlleBerichten(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> getBerichtById(@PathVariable Long id) {
        return berichtService.getBerichtById(id)
                .map(bericht -> new ResponseEntity<>(bericht, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Message> verstuurBericht(@RequestBody Message bericht) {
        Message nieuwBericht = berichtService.verstuurBericht(bericht);
        return new ResponseEntity<>(nieuwBericht, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/markeer-gelezen")
    public ResponseEntity<Message> markeerAlsGelezen(@PathVariable Long id) {
        try {
            Message gelezenBericht = berichtService.markeerAlsGelezen(id);
            return new ResponseEntity<>(gelezenBericht, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderBericht(@PathVariable Long id) {
        try {
            berichtService.verwijderBericht(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/ontvangen/{gebruikerId}")
    public ResponseEntity<List<Message>> getOntvangenBerichtenVoorGebruiker(@PathVariable Long gebruikerId) {
        try {
            List<Message> ontvangenBerichten = berichtService.getOntvangenBerichtenVoorGebruiker(gebruikerId);
            return ResponseEntity.ok(ontvangenBerichten);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/verzonden/{gebruikerId}")
    public ResponseEntity<List<Message>> getVerzondenBerichtenVanGebruiker(@PathVariable Long gebruikerId) {
        try {
            List<Message> verzondenBerichten = berichtService.getVerzondenBerichtenVanGebruiker(gebruikerId);
            return ResponseEntity.ok(verzondenBerichten);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ongelezen/{gebruikerId}")
    public ResponseEntity<List<Message>> getOngelezenBerichtenVoorGebruiker(@PathVariable Long gebruikerId) {
        try {
            List<Message> ongelezenBerichten = berichtService.getOngelezenBerichtenVoorGebruiker(gebruikerId);
            return ResponseEntity.ok(ongelezenBerichten);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}