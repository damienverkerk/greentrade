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

import com.greentrade.greentrade.dto.CertificateDTO;
import com.greentrade.greentrade.services.CertificateService;

@RestController
@RequestMapping("/api/certificaten")
public class CertificateController {

    private final CertificateService certificateService;

    @Autowired
    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping
    public ResponseEntity<List<CertificateDTO>> getAlleCertificaten() {
        return new ResponseEntity<>(certificateService.getAlleCertificaten(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateDTO> getCertificaatById(@PathVariable Long id) {
        CertificateDTO certificate = certificateService.getCertificaatById(id);
        return certificate != null ? ResponseEntity.ok(certificate) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<CertificateDTO> maakCertificaat(@RequestBody CertificateDTO certificateDTO) {
        try {
            CertificateDTO nieuwCertificaat = certificateService.maakCertificaat(certificateDTO);
            return new ResponseEntity<>(nieuwCertificaat, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificateDTO> updateCertificaat(@PathVariable Long id, 
                                                           @RequestBody CertificateDTO certificateDTO) {
        try {
            CertificateDTO updatedCertificaat = certificateService.updateCertificaat(id, certificateDTO);
            return ResponseEntity.ok(updatedCertificaat);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderCertificaat(@PathVariable Long id) {
        try {
            certificateService.verwijderCertificaat(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/gebruiker/{userId}")
    public ResponseEntity<List<CertificateDTO>> getCertificatenVanGebruiker(@PathVariable Long userId) {
        try {
            List<CertificateDTO> certificaten = certificateService.getCertificatenVanGebruiker(userId);
            return ResponseEntity.ok(certificaten);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/verlopen")
    public ResponseEntity<List<CertificateDTO>> getVerlopenCertificaten(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum) {
        List<CertificateDTO> verlopenCertificaten = certificateService.getVerlopenCertificaten(datum);
        return ResponseEntity.ok(verlopenCertificaten);
    }
}