package com.greentrade.greentrade.controllers;

import com.greentrade.greentrade.dto.ProductDTO;
import com.greentrade.greentrade.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ProductDTO> maakProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO nieuwProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(nieuwProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
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