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
@RequestMapping("/api/products")
@Tag(name = "Products", description = "API endpoints for product management")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(
        summary = "Get all products",
        description = "Retrieves a list of all available products in the system"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Products successfully retrieved",
            content = @Content(schema = @Schema(implementation = ProductDTO.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    @Operation(
        summary = "Get a specific product",
        description = "Retrieves a specific product based on its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product successfully found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return product != null ? new ResponseEntity<>(product, HttpStatus.OK) 
                             : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(
        summary = "Create a new product",
        description = "Creates a new product in the system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @Parameter(description = "Product data", required = true)
            @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO newProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Update an existing product",
        description = "Updates an existing product with new data"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product successfully updated"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated product data", required = true)
            @Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
        summary = "Delete a product",
        description = "Deletes a product from the system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
        summary = "Search products by name",
        description = "Searches products based on a name (partial match)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search successfully executed")
    })
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @Parameter(description = "Name to search for", required = true)
            @RequestParam String name) {
        List<ProductDTO> products = productService.searchProductsByName(name);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @Operation(
        summary = "Get sustainable products",
        description = "Retrieves products with a minimum sustainability score"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Products successfully retrieved")
    })
    @GetMapping("/sustainable")
    public ResponseEntity<List<ProductDTO>> getSustainableProducts(
            @Parameter(description = "Minimum sustainability score", required = true)
            @RequestParam Integer minimumScore) {
        List<ProductDTO> products = productService.getProductsBySustainabilityScore(minimumScore);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }
}