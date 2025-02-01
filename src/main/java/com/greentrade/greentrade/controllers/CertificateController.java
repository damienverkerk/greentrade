package com.greentrade.greentrade.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.greentrade.greentrade.config.FileValidationConfig;
import com.greentrade.greentrade.dto.CertificateDTO;
import com.greentrade.greentrade.exception.file.FileStorageException;
import com.greentrade.greentrade.exception.file.InvalidFileException;
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

@RestController
@RequestMapping("/api/certificaten")
@Tag(name = "Certificaten", description = "API endpoints voor het beheren van duurzaamheidscertificaten")
public class CertificateController {

    private final CertificateService certificateService;
    private final FileStorageService fileStorageService;
    private final FileValidationConfig fileValidationConfig;

    @Autowired
    public CertificateController(CertificateService certificateService, 
                               FileStorageService fileStorageService,
                               FileValidationConfig fileValidationConfig) {
        this.certificateService = certificateService;
        this.fileStorageService = fileStorageService;
        this.fileValidationConfig = fileValidationConfig;
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
        List<CertificateDTO> certificaten = certificateService.getAlleCertificaten();
        return ResponseEntity.ok(certificaten);
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
        description = "Upload een PDF of afbeeldingsbestand voor een certificaat. " +
                     "Toegestane bestandstypen: PDF, JPG, PNG."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bestand succesvol geüpload"),
        @ApiResponse(responseCode = "400", description = "Ongeldig bestand of bestandsformaat"),
        @ApiResponse(responseCode = "404", description = "Certificaat niet gevonden")
    })
    @PostMapping("/{id}/bestand")
    public ResponseEntity<CertificateDTO> uploadCertificaatBestand(
        @PathVariable Long id,
        @RequestParam("bestand") MultipartFile bestand) {
        try {
            CertificateDTO certificate = certificateService.getCertificaatById(id);
            if (certificate == null) {
                return ResponseEntity.notFound().build();
            }

            fileStorageService.validateFileType(
                bestand, 
                fileValidationConfig.getAllowedExtensions().toArray(String[]::new)
            );
            
            String bestandsNaam = fileStorageService.storeFile(bestand);
            CertificateDTO updatedCertificate = certificateService.updateCertificaatBestand(id, bestandsNaam);
            
            return ResponseEntity.ok(updatedCertificate);
        } catch (InvalidFileException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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
            String contentType = determineContentType(certificate.getBestandsPad());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (FileStorageException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bestand niet gevonden");
        }
    }

    @Operation(
        summary = "Verwijder een certificaat bestand",
        description = "Verwijdert het bestand dat bij een certificaat hoort"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Bestand succesvol verwijderd"),
        @ApiResponse(responseCode = "404", description = "Bestand niet gevonden")
    })
    @DeleteMapping("/{id}/bestand")
    public ResponseEntity<Void> verwijderCertificaatBestand(
            @Parameter(description = "ID van het certificaat", required = true)
            @PathVariable Long id) {
        try {
            CertificateDTO certificate = certificateService.getCertificaatById(id);
            if (certificate == null || certificate.getBestandsPad() == null) {
                return ResponseEntity.notFound().build();
            }

            fileStorageService.deleteFile(certificate.getBestandsPad());
            certificateService.updateCertificaatBestand(id, null);
            
            return ResponseEntity.noContent().build();
        } catch (FileStorageException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bestand niet gevonden");
        }
    }

    private String determineContentType(String fileName) {
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (fileExtension) {
            case "pdf" -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> "application/octet-stream";
        };
    }
}