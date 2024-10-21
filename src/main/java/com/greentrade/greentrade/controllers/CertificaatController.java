package com.greentrade.greentrade.controllers;

import java.time.LocalDate;
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

import com.greentrade.greentrade.models.Certificate;
import com.greentrade.greentrade.services.CertificaatService;

@RestController
@RequestMapping("/api/certificaten")
public class CertificaatController {

    private final CertificaatService certificaatService;

    @Autowired
    public CertificaatController(CertificaatService certificaatService) {
        this.certificaatService = certificaatService;
    }

    @GetMapping
    public ResponseEntity<List<Certificate>> getAlleCertificaten() {
        return new ResponseEntity<>(certificaatService.getAlleCertificaten(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Certificate> getCertificaatById(@PathVariable Long id) {
        return certificaatService.getCertificaatById(id)
                .map(certificaat -> new ResponseEntity<>(certificaat, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Certificate> maakCertificaat(@RequestBody Certificate certificaat) {
        Certificate nieuwCertificaat = certificaatService.maakCertificaat(certificaat);
        return new ResponseEntity<>(nieuwCertificaat, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Certificate> updateCertificaat(@PathVariable Long id, @RequestBody Certificate certificaat) {
        try {
            Certificate updatedCertificaat = certificaatService.updateCertificaat(id, certificaat);
            return new ResponseEntity<>(updatedCertificaat, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderCertificaat(@PathVariable Long id) {
        try {
            certificaatService.verwijderCertificaat(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/verlopen")
    public ResponseEntity<List<Certificate>> getVerlopenCertificaten(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum) {
        List<Certificate> verlopenCertificaten = certificaatService.getVerlopenCertificaten(datum);
        return new ResponseEntity<>(verlopenCertificaten, HttpStatus.OK);
    }
}
