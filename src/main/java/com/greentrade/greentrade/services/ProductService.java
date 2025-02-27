package com.greentrade.greentrade.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.ProductDTO;
import com.greentrade.greentrade.exception.product.InvalidProductDataException;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.ProductRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<ProductDTO> getProductsBySeller(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException(sellerId));
        return productRepository.findBySeller(seller).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProductsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductDataException("name", "Search query cannot be empty");
        }
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsBySustainabilityScore(Integer minimumScore) {
        if (minimumScore == null || minimumScore < 0 || minimumScore > 100) {
            throw new InvalidProductDataException("sustainabilityScore", 
                "Score must be between 0 and 100");
        }
        return productRepository.findBySustainabilityScoreGreaterThanEqual(minimumScore).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        validateProductData(productDTO);
        
        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }

        validateProductData(productDTO);
        
        return productRepository.findById(id)
                .map(product -> {
                    updateProductFromDTO(product, productDTO);
                    return convertToDTO(productRepository.save(product));
                })
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
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

    private ProductDTO convertToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSustainabilityScore(),
                product.getSustainabilityCertificate(),
                product.getSeller().getId()
        );
    }

    private Product convertToEntity(ProductDTO productDTO) {
        Product product = new Product();
        return updateProductFromDTO(product, productDTO);
    }

    private Product updateProductFromDTO(Product product, ProductDTO productDTO) {
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setSustainabilityScore(productDTO.getSustainabilityScore());
        product.setSustainabilityCertificate(productDTO.getSustainabilityCertificate());
        
        if (productDTO.getSellerId() != null) {
            User seller = userRepository.findById(productDTO.getSellerId())
                    .orElseThrow(() -> new UserNotFoundException(productDTO.getSellerId()));
            product.setSeller(seller);
        }
        
        return product;
    }
}