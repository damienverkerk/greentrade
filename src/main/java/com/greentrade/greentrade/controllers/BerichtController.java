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

import com.greentrade.greentrade.models.Message;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.services.BerichtService;
import com.greentrade.greentrade.services.UserService;

@RestController
@RequestMapping("/api/berichten")
public class BerichtController {

    private final BerichtService berichtService;
    private final UserService userService;

    @Autowired
    public BerichtController(BerichtService berichtService, UserService userService) {
        this.berichtService = berichtService;
        this.userService = userService;
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
        User gebruiker = userService.getUserById(gebruikerId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden met id: " + gebruikerId));
        List<Message> ontvangenBerichten = berichtService.getOntvangenBerichtenVoorGebruiker(gebruiker);
        return new ResponseEntity<>(ontvangenBerichten, HttpStatus.OK);
    }

    @GetMapping("/verzonden/{gebruikerId}")
    public ResponseEntity<List<Message>> getVerzondenBerichtenVanGebruiker(@PathVariable Long gebruikerId) {
        User gebruiker = userService.getUserById(gebruikerId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden met id: " + gebruikerId));
        List<Message> verzondenBerichten = berichtService.getVerzondenBerichtenVanGebruiker(gebruiker);
        return new ResponseEntity<>(verzondenBerichten, HttpStatus.OK);
    }

    @GetMapping("/ongelezen/{gebruikerId}")
    public ResponseEntity<List<Message>> getOngelezenBerichtenVoorGebruiker(@PathVariable Long gebruikerId) {
        User gebruiker = userService.getUserById(gebruikerId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden met id: " + gebruikerId));
        List<Message> ongelezenBerichten = berichtService.getOngelezenBerichtenVoorGebruiker(gebruiker);
        return new ResponseEntity<>(ongelezenBerichten, HttpStatus.OK);
    }
}