package com.greentrade.greentrade.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.ProductDTO;
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

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDTO)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<ProductDTO> getProductsBySeller(Long sellerId) {
        User seller = findSellerById(sellerId);
        return productRepository.findBySeller(seller).stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProductsByName(String name) {
        validateSearchQuery(name);
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsBySustainabilityScore(Integer minimumScore) {
        validateSustainabilityScore(minimumScore);
        return productRepository.findBySustainabilityScoreGreaterThanEqual(minimumScore).stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        validateProductData(productDTO);
        User seller = findSellerById(productDTO.getSellerId());
        Product product = productMapper.toEntity(productDTO, seller);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDTO(savedProduct);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }

        validateProductData(productDTO);
        User seller = findSellerById(productDTO.getSellerId());
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        updateProductFields(product, productDTO, seller);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toDTO(updatedProduct);
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

    private void validateProductData(ProductDTO productDTO) {
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new InvalidProductDataException("name", "Name is required");
        }
        if (productDTO.getPrice() == null || productDTO.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("price", "Price must be greater than 0");
        }
        if (productDTO.getSustainabilityScore() != null && 
            (productDTO.getSustainabilityScore() < 0 || productDTO.getSustainabilityScore() > 100)) {
            throw new InvalidProductDataException("sustainabilityScore", 
                "Sustainability score must be between 0 and 100");
        }
    }
    
    private void updateProductFields(Product product, ProductDTO productDTO, User seller) {
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setSustainabilityScore(productDTO.getSustainabilityScore());
        product.setSustainabilityCertificate(productDTO.getSustainabilityCertificate());
        product.setSeller(seller);
    }
}