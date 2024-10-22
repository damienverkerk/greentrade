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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/producten")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAlleProducten() {
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return product != null ? new ResponseEntity<>(product, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> maakProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO nieuwProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(nieuwProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/zoek")
    public ResponseEntity<List<ProductDTO>> zoekProducten(@RequestParam String naam) {
        List<ProductDTO> producten = productService.searchProductsByName(naam);
        return new ResponseEntity<>(producten, HttpStatus.OK);
    }

    @GetMapping("/duurzaam")
    public ResponseEntity<List<ProductDTO>> getDuurzameProducten(@RequestParam Integer minimumScore) {
        List<ProductDTO> producten = productService.getProductsByDuurzaamheidsScore(minimumScore);
        return new ResponseEntity<>(producten, HttpStatus.OK);
    }

    @GetMapping("/verkoper/{verkoperId}")
    public ResponseEntity<List<ProductDTO>> getProductenVanVerkoper(@PathVariable Long verkoperId) {
        List<ProductDTO> producten = productService.getProductsByVerkoper(verkoperId);
        return new ResponseEntity<>(producten, HttpStatus.OK);
    }
}