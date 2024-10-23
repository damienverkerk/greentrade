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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greentrade.greentrade.dto.ProductDTO;
import com.greentrade.greentrade.services.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/producten")
@Tag(name = "Producten", description = "API endpoints voor product management")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(
        summary = "Haal alle producten op",
        description = "Haalt een lijst van alle beschikbare producten op in het systeem"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Producten succesvol opgehaald",
            content = @Content(schema = @Schema(implementation = ProductDTO.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAlleProducten() {
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    @Operation(
        summary = "Haal een specifiek product op",
        description = "Haalt een specifiek product op basis van het ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product succesvol gevonden"),
        @ApiResponse(responseCode = "404", description = "Product niet gevonden")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "ID van het product", required = true)
            @PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return product != null ? new ResponseEntity<>(product, HttpStatus.OK) 
                             : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(
        summary = "Maak een nieuw product aan",
        description = "Maakt een nieuw product aan in het systeem"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product succesvol aangemaakt"),
        @ApiResponse(responseCode = "400", description = "Ongeldige invoergegevens")
    })
    @PostMapping
    public ResponseEntity<ProductDTO> maakProduct(
            @Parameter(description = "Product gegevens", required = true)
            @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO nieuwProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(nieuwProduct, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Update een bestaand product",
        description = "Werkt een bestaand product bij met nieuwe gegevens"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product succesvol bijgewerkt"),
        @ApiResponse(responseCode = "404", description = "Product niet gevonden"),
        @ApiResponse(responseCode = "400", description = "Ongeldige invoergegevens")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "ID van het product", required = true)
            @PathVariable Long id,
            @Parameter(description = "Bijgewerkte product gegevens", required = true)
            @Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
        summary = "Verwijder een product",
        description = "Verwijdert een product uit het systeem"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product succesvol verwijderd"),
        @ApiResponse(responseCode = "404", description = "Product niet gevonden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderProduct(
            @Parameter(description = "ID van het product", required = true)
            @PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
        summary = "Zoek producten op naam",
        description = "Zoekt producten op basis van een naam (gedeeltelijke match)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Zoekopdracht succesvol uitgevoerd")
    })
    @GetMapping("/zoek")
    public ResponseEntity<List<ProductDTO>> zoekProducten(
            @Parameter(description = "Naam om op te zoeken", required = true)
            @RequestParam String naam) {
        List<ProductDTO> producten = productService.searchProductsByName(naam);
        return new ResponseEntity<>(producten, HttpStatus.OK);
    }

    @Operation(
        summary = "Haal duurzame producten op",
        description = "Haalt producten op met een minimale duurzaamheidsscore"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producten succesvol opgehaald")
    })
    @GetMapping("/duurzaam")
    public ResponseEntity<List<ProductDTO>> getDuurzameProducten(
            @Parameter(description = "Minimale duurzaamheidsscore", required = true)
            @RequestParam Integer minimumScore) {
        List<ProductDTO> producten = productService.getProductsByDuurzaamheidsScore(minimumScore);
        return new ResponseEntity<>(producten, HttpStatus.OK);
    }
}