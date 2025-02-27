// src/main/java/com/greentrade/greentrade/services/ProductService.java
package com.greentrade.greentrade.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.product.ProductCreateRequest;
import com.greentrade.greentrade.dto.product.ProductResponse;
import com.greentrade.greentrade.dto.product.ProductUpdateRequest;
import com.greentrade.greentrade.exception.product.InvalidProductDataException;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
import com.greentrade.greentrade.mappers.ProductMapper;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.ProductRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductService(
            ProductRepository productRepository, 
            UserRepository userRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productMapper = productMapper;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<ProductResponse> getProductsBySeller(Long sellerId) {
        User seller = findSellerById(sellerId);
        return productRepository.findBySeller(seller).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchProductsByName(String name) {
        validateSearchQuery(name);
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsBySustainabilityScore(Integer minimumScore) {
        validateSustainabilityScore(minimumScore);
        return productRepository.findBySustainabilityScoreGreaterThanEqual(minimumScore).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse createProduct(ProductCreateRequest request) {
        validateProductData(request);
        User seller = findSellerById(request.getSellerId());
        Product product = productMapper.createRequestToEntity(request, seller);
        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }

        validateProductData(request);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        User seller = null;
        if (request.getSellerId() != null) {
            seller = findSellerById(request.getSellerId());
        }
        
        productMapper.updateEntityFromRequest(product, request, seller);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }
    
    private User findSellerById(Long sellerId) {
        return userRepository.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException(sellerId));
    }
    
    private void validateSearchQuery(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductDataException("name", "Search query cannot be empty");
        }
    }
    
    private void validateSustainabilityScore(Integer minimumScore) {
        if (minimumScore == null || minimumScore < 0 || minimumScore > 100) {
            throw new InvalidProductDataException("sustainabilityScore", 
                "Score must be between 0 and 100");
        }
    }

    private void validateProductData(ProductCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidProductDataException("name", "Name is required");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("price", "Price must be greater than 0");
        }
        if (request.getSustainabilityScore() != null && 
            (request.getSustainabilityScore() < 0 || request.getSustainabilityScore() > 100)) {
            throw new InvalidProductDataException("sustainabilityScore", 
                "Sustainability score must be between 0 and 100");
        }
    }
    
    private void validateProductData(ProductUpdateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidProductDataException("name", "Name is required");
        }
        if (request.getPrice() != null && request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("price", "Price must be greater than 0");
        }
        if (request.getSustainabilityScore() != null && 
            (request.getSustainabilityScore() < 0 || request.getSustainabilityScore() > 100)) {
            throw new InvalidProductDataException("sustainabilityScore", 
                "Sustainability score must be between 0 and 100");
        }
    }
}