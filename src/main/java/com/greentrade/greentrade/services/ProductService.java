package com.greentrade.greentrade.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByVerkoper(User verkoper) {
        return productRepository.findByVerkoper(verkoper);
    }

    public List<Product> searchProductsByName(String naam) {
        return productRepository.findByNaamContainingIgnoreCase(naam);
    }

    public List<Product> getProductsByDuurzaamheidsScore(Integer minimumScore) {
        return productRepository.findByDuurzaamheidsScoreGreaterThanEqual(minimumScore);
    }

    public Product createProduct(Product product) {
        // Hier kunt u eventueel extra validatie toevoegen
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product niet gevonden met id: " + id));

        product.setNaam(productDetails.getNaam());
        product.setBeschrijving(productDetails.getBeschrijving());
        product.setPrijs(productDetails.getPrijs());
        product.setDuurzaamheidsScore(productDetails.getDuurzaamheidsScore());
        product.setDuurzaamheidsCertificaat(productDetails.getDuurzaamheidsCertificaat());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product niet gevonden met id: " + id);
        }
        productRepository.deleteById(id);
    }
}