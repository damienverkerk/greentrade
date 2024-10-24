package com.greentrade.greentrade.controllers;

import com.greentrade.greentrade.dto.CertificateDTO;
import com.greentrade.greentrade.services.CertificateService;
import com.greentrade.greentrade.services.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/certificaten")
@Tag(name = "Certificaten", description = "API endpoints voor het beheren van duurzaamheidscertificaten")
public class CertificateController {

    private final CertificateService certificateService;
    private final FileStorageService fileStorageService;

    @Autowired
    public CertificateController(CertificateService certificateService, FileStorageService fileStorageService) {
        this.certificateService = certificateService;
        this.fileStorageService = fileStorageService;
    }

    @Operation(
        summary = "Haal alle certificaten op",
        description = "Haalt een lijst van alle duurzaamheidscertificaten op in het systeem"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Certificaten succesvol opgehaald",
            content = @Content(schema = @Schema(implementation = CertificateDTO.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<CertificateDTO>> getAlleCertificaten() {
        return ResponseEntity.ok(certificateService.getAlleCertificaten());
    }

    @Operation(
        summary = "Haal een specifiek certificaat op",
        description = "Haalt een specifiek certificaat op basis van het ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Certificaat succesvol gevonden"),
        @ApiResponse(responseCode = "404", description = "Certificaat niet gevonden")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CertificateDTO> getCertificaatById(
            @Parameter(description = "ID van het certificaat", required = true)
            @PathVariable Long id) {
        CertificateDTO certificate = certificateService.getCertificaatById(id);
        return certificate != null ? ResponseEntity.ok(certificate) : ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Maak een nieuw certificaat aan",
        description = "Registreert een nieuw duurzaamheidscertificaat in het systeem"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Certificaat succesvol aangemaakt"),
        @ApiResponse(responseCode = "400", description = "Ongeldige invoergegevens")
    })
    @PostMapping
    public ResponseEntity<CertificateDTO> maakCertificaat(
            @Parameter(description = "Certificaat gegevens", required = true)
            @Valid @RequestBody CertificateDTO certificateDTO) {
        try {
            CertificateDTO nieuwCertificaat = certificateService.maakCertificaat(certificateDTO);
            return ResponseEntity.ok(nieuwCertificaat);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Update een bestaand certificaat",
        description = "Werkt een bestaand certificaat bij met nieuwe gegevens"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Certificaat succesvol bijgewerkt"),
        @ApiResponse(responseCode = "404", description = "Certificaat niet gevonden"),
        @ApiResponse(responseCode = "400", description = "Ongeldige invoergegevens")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CertificateDTO> updateCertificaat(
            @Parameter(description = "ID van het certificaat", required = true)
            @PathVariable Long id,
            @Parameter(description = "Bijgewerkte certificaat gegevens", required = true)
            @Valid @RequestBody CertificateDTO certificateDTO) {
        try {
            CertificateDTO updatedCertificaat = certificateService.updateCertificaat(id, certificateDTO);
            return ResponseEntity.ok(updatedCertificaat);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Verwijder een certificaat",
        description = "Verwijdert een certificaat uit het systeem"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Certificaat succesvol verwijderd"),
        @ApiResponse(responseCode = "404", description = "Certificaat niet gevonden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderCertificaat(
            @Parameter(description = "ID van het certificaat", required = true)
            @PathVariable Long id) {
        try {
            certificateService.verwijderCertificaat(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Haal certificaten van een gebruiker op",
        description = "Haalt alle certificaten op die gekoppeld zijn aan een specifieke gebruiker"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Certificaten succesvol opgehaald"),
        @ApiResponse(responseCode = "404", description = "Gebruiker niet gevonden")
    })
    @GetMapping("/gebruiker/{userId}")
    public ResponseEntity<List<CertificateDTO>> getCertificatenVanGebruiker(
            @Parameter(description = "ID van de gebruiker", required = true)
            @PathVariable Long userId) {
        try {
            List<CertificateDTO> certificaten = certificateService.getCertificatenVanGebruiker(userId);
            return ResponseEntity.ok(certificaten);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Haal verlopen certificaten op",
        description = "Haalt alle certificaten op die verlopen zijn vóór een bepaalde datum"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Verlopen certificaten succesvol opgehaald")
    })
    @GetMapping("/verlopen")
    public ResponseEntity<List<CertificateDTO>> getVerlopenCertificaten(
            @Parameter(description = "Referentiedatum (ISO format)", required = true, example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum) {
        List<CertificateDTO> verlopenCertificaten = certificateService.getVerlopenCertificaten(datum);
        return ResponseEntity.ok(verlopenCertificaten);
    }

    @Operation(
        summary = "Upload een certificaat bestand",
        description = "Upload een PDF of afbeeldingsbestand voor een certificaat"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bestand succesvol geüpload"),
        @ApiResponse(responseCode = "400", description = "Ongeldig bestand")
    })
    @PostMapping("/{id}/bestand")
    public ResponseEntity<CertificateDTO> uploadCertificaatBestand(
            @Parameter(description = "ID van het certificaat", required = true)
            @PathVariable Long id,
            @Parameter(description = "Bestand om te uploaden", required = true)
            @RequestParam("bestand") MultipartFile bestand) {
        try {
            String bestandsNaam = fileStorageService.storeFile(bestand);
            return ResponseEntity.ok(certificateService.updateCertificaatBestand(id, bestandsNaam));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Download een certificaat bestand",
        description = "Download het bestand dat bij een certificaat hoort"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bestand succesvol gedownload"),
        @ApiResponse(responseCode = "404", description = "Bestand niet gevonden")
    })
    @GetMapping("/{id}/bestand")
    public ResponseEntity<Resource> downloadCertificaatBestand(
            @Parameter(description = "ID van het certificaat", required = true)
            @PathVariable Long id) {
        try {
            CertificateDTO certificate = certificateService.getCertificaatById(id);
            if (certificate == null || certificate.getBestandsPad() == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = fileStorageService.loadFileAsResource(certificate.getBestandsPad());
            String contentType = "application/octet-stream";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}