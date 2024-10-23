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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/beoordelingen")
@Tag(name = "Beoordelingen", description = "API endpoints voor het beheren van product beoordelingen")
public class BeoordelingController {

    private final BeoordelingService beoordelingService;

    @Autowired
    public BeoordelingController(BeoordelingService beoordelingService) {
        this.beoordelingService = beoordelingService;
    }

    @Operation(
        summary = "Haal alle beoordelingen op",
        description = "Haalt een lijst van alle beoordelingen op in het systeem"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Beoordelingen succesvol opgehaald",
            content = @Content(schema = @Schema(implementation = ReviewDTO.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAlleBeoordelingen() {
        return new ResponseEntity<>(beoordelingService.getAlleBeoordelingen(), HttpStatus.OK);
    }

    @Operation(
        summary = "Haal een specifieke beoordeling op",
        description = "Haalt een specifieke beoordeling op basis van het ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Beoordeling succesvol gevonden"),
        @ApiResponse(responseCode = "404", description = "Beoordeling niet gevonden")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getBeoordelingById(
            @Parameter(description = "ID van de beoordeling", required = true)
            @PathVariable Long id) {
        ReviewDTO review = beoordelingService.getBeoordelingById(id);
        return review != null ? ResponseEntity.ok(review) : ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Maak een nieuwe beoordeling",
        description = "Maakt een nieuwe beoordeling aan voor een product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Beoordeling succesvol aangemaakt"),
        @ApiResponse(responseCode = "400", description = "Ongeldige invoergegevens")
    })
    @PostMapping
    public ResponseEntity<ReviewDTO> maakBeoordeling(
            @Parameter(description = "Beoordeling gegevens", required = true)
            @Valid @RequestBody ReviewDTO reviewDTO) {
        ReviewDTO nieuweBeoordeling = beoordelingService.maakBeoordeling(reviewDTO);
        return new ResponseEntity<>(nieuweBeoordeling, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Update een bestaande beoordeling",
        description = "Werkt een bestaande beoordeling bij met nieuwe gegevens"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Beoordeling succesvol bijgewerkt"),
        @ApiResponse(responseCode = "404", description = "Beoordeling niet gevonden"),
        @ApiResponse(responseCode = "400", description = "Ongeldige invoergegevens")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateBeoordeling(
            @Parameter(description = "ID van de beoordeling", required = true)
            @PathVariable Long id,
            @Parameter(description = "Bijgewerkte beoordeling gegevens", required = true)
            @Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            ReviewDTO updatedBeoordeling = beoordelingService.updateBeoordeling(id, reviewDTO);
            return ResponseEntity.ok(updatedBeoordeling);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Verwijder een beoordeling",
        description = "Verwijdert een beoordeling uit het systeem"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Beoordeling succesvol verwijderd"),
        @ApiResponse(responseCode = "404", description = "Beoordeling niet gevonden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderBeoordeling(
            @Parameter(description = "ID van de beoordeling", required = true)
            @PathVariable Long id) {
        try {
            beoordelingService.verwijderBeoordeling(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Haal beoordelingen op voor een product",
        description = "Haalt alle beoordelingen op voor een specifiek product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Beoordelingen succesvol opgehaald"),
        @ApiResponse(responseCode = "404", description = "Product niet gevonden")
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getBeoordelingenVoorProduct(
            @Parameter(description = "ID van het product", required = true)
            @PathVariable Long productId) {
        try {
            List<ReviewDTO> beoordelingen = beoordelingService.getBeoordelingenVoorProduct(productId);
            return ResponseEntity.ok(beoordelingen);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Haal gemiddelde score op voor een product",
        description = "Berekent en haalt de gemiddelde beoordelingsscore op voor een specifiek product"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Gemiddelde score succesvol berekend"),
        @ApiResponse(responseCode = "404", description = "Product niet gevonden")
    })
    @GetMapping("/gemiddelde-score/product/{productId}")
    public ResponseEntity<Double> getGemiddeldeScoreVoorProduct(
            @Parameter(description = "ID van het product", required = true)
            @PathVariable Long productId) {
        try {
            Double gemiddeldeScore = beoordelingService.getGemiddeldeScoreVoorProduct(productId);
            return ResponseEntity.ok(gemiddeldeScore);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}