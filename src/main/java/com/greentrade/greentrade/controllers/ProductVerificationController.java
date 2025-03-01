package com.greentrade.greentrade.controllers;

import java.net.URI;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.greentrade.greentrade.dto.verification.VerificationResponse;
import com.greentrade.greentrade.dto.verification.VerificationReviewRequest;
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
@Tag(name = "Product Verifications", description = "Endpoints for managing product verifications")
public class ProductVerificationController {
    private final ProductVerificationService verificationService;

    public ProductVerificationController(ProductVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @Operation(
        summary = "Submit product for verification",
        description = "Sellers can submit their products for sustainability verification"
    )
    @ApiResponse(responseCode = "201", description = "Product successfully submitted for verification")
    @ApiResponse(responseCode = "400", description = "Product already has a pending verification")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PostMapping("/products/{productId}/submit")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<VerificationResponse> submitForVerification(@PathVariable Long productId) {
        try {
            VerificationResponse verification = verificationService.submitForVerification(productId);
            
            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/api/verifications/{id}")
                    .buildAndExpand(verification.getId())
                    .toUri();
            
            return ResponseEntity.created(location).body(verification);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (DuplicateVerificationException e) {
            throw e;  // Let this be handled by the global exception handler
        }
    }
    
    @Operation(
        summary = "Review product verification",
        description = "Administrators can review submitted products"
    )
    @ApiResponse(responseCode = "200", description = "Verification successfully reviewed")
    @ApiResponse(responseCode = "400", description = "Invalid review data")
    @ApiResponse(responseCode = "404", description = "Verification not found")
    @PostMapping("/{verificationId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VerificationResponse> reviewProduct(
            @Parameter(description = "ID of the verification", required = true)
            @PathVariable Long verificationId,
            @Parameter(description = "Review data", required = true)
            @Valid @RequestBody VerificationReviewRequest dto,
            @Parameter(description = "ID of the reviewer", required = true)
            @RequestParam Long reviewerId) {
        
        // Let's ensure we return the correct HTTP status
        VerificationResponse response = verificationService.reviewProduct(verificationId, dto, reviewerId);
        
        // For review operations, we return 200 OK, not 201 Created
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get pending verifications",
        description = "Shows all verifications with PENDING status"
    )
    @ApiResponse(responseCode = "200", description = "Pending verifications successfully retrieved")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VerificationResponse>> getPendingVerifications() {
        return ResponseEntity.ok(verificationService.getPendingVerifications());
    }

    @Operation(
        summary = "Get verifications for a product",
        description = "Shows all verifications for a specific product"
    )
    @ApiResponse(responseCode = "200", description = "Verifications successfully retrieved")
    @GetMapping("/products/{productId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<List<VerificationResponse>> getVerificationsByProduct(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long productId) {
        return ResponseEntity.ok(verificationService.getVerificationsByProduct(productId));
    }
}