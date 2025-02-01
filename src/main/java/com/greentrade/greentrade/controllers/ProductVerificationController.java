package com.greentrade.greentrade.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greentrade.greentrade.dto.ProductVerificationDTO;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
import com.greentrade.greentrade.exception.verification.DuplicateVerificationException;
import com.greentrade.greentrade.services.ProductVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/verifications")
@Tag(name = "Product Verificaties", description = "Endpoints voor het beheren van product verificaties")
public class ProductVerificationController {
    private final ProductVerificationService verificationService;

    public ProductVerificationController(ProductVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @Operation(
        summary = "Dien product in voor verificatie",
        description = "Verkopers kunnen hun producten indienen voor duurzaamheidsverificatie"
    )
    @ApiResponse(responseCode = "200", description = "Product succesvol ingediend voor verificatie")
    @ApiResponse(responseCode = "400", description = "Product heeft al een lopende verificatie")
    @ApiResponse(responseCode = "404", description = "Product niet gevonden")
    @PostMapping("/products/{productId}/submit")
    @PreAuthorize("hasRole('VERKOPER')")
    public ResponseEntity<ProductVerificationDTO> submitForVerification(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(verificationService.submitForVerification(productId));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (DuplicateVerificationException e) {
            throw e;  // Laat deze doorgaan naar de global exception handler
        }
    }
    @Operation(
        summary = "Beoordeel product verificatie",
        description = "Administrators kunnen ingediende producten beoordelen"
    )
    @ApiResponse(responseCode = "200", description = "Verificatie succesvol beoordeeld")
    @ApiResponse(responseCode = "400", description = "Ongeldige beoordelingsgegevens")
    @ApiResponse(responseCode = "404", description = "Verificatie niet gevonden")
    @PostMapping("/{verificationId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductVerificationDTO> reviewProduct(
            @Parameter(description = "ID van de verificatie", required = true)
            @PathVariable Long verificationId,
            @Parameter(description = "Beoordelingsgegevens", required = true)
            @Valid @RequestBody ProductVerificationDTO dto,
            @Parameter(description = "ID van de beoordelaar", required = true)
            @RequestParam Long reviewerId) {
        return ResponseEntity.ok(verificationService.reviewProduct(verificationId, dto, reviewerId));
    }

    @Operation(
        summary = "Haal openstaande verificaties op",
        description = "Toont alle verificaties met status PENDING"
    )
    @ApiResponse(responseCode = "200", description = "Openstaande verificaties succesvol opgehaald")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductVerificationDTO>> getPendingVerifications() {
        return ResponseEntity.ok(verificationService.getPendingVerifications());
    }

    @Operation(
        summary = "Haal verificaties op voor een product",
        description = "Toont alle verificaties voor een specifiek product"
    )
    @ApiResponse(responseCode = "200", description = "Verificaties succesvol opgehaald")
    @GetMapping("/products/{productId}")
    @PreAuthorize("hasAnyRole('VERKOPER', 'ADMIN')")
    public ResponseEntity<List<ProductVerificationDTO>> getVerificationsByProduct(
            @Parameter(description = "ID van het product", required = true)
            @PathVariable Long productId) {
        return ResponseEntity.ok(verificationService.getVerificationsByProduct(productId));
    }
}