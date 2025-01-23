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

    public List<ProductDTO> getProductsByVerkoper(Long verkoperId) {
        User verkoper = userRepository.findById(verkoperId)
                .orElseThrow(() -> new UserNotFoundException(verkoperId));
        return productRepository.findByVerkoper(verkoper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProductsByName(String naam) {
        if (naam == null || naam.trim().isEmpty()) {
            throw new InvalidProductDataException("naam", "Zoekopdracht mag niet leeg zijn");
        }
        return productRepository.findByNaamContainingIgnoreCase(naam).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByDuurzaamheidsScore(Integer minimumScore) {
        if (minimumScore == null || minimumScore < 0 || minimumScore > 100) {
            throw new InvalidProductDataException("duurzaamheidsScore", 
                "Score moet tussen 0 en 100 liggen");
        }
        return productRepository.findByDuurzaamheidsScoreGreaterThanEqual(minimumScore).stream()
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
        if (productDTO.getNaam() == null || productDTO.getNaam().trim().isEmpty()) {
            throw new InvalidProductDataException("naam", "Naam is verplicht");
        }
        if (productDTO.getPrijs() == null || productDTO.getPrijs().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("prijs", "Prijs moet groter zijn dan 0");
        }
        if (productDTO.getDuurzaamheidsScore() != null && 
            (productDTO.getDuurzaamheidsScore() < 0 || productDTO.getDuurzaamheidsScore() > 100)) {
            throw new InvalidProductDataException("duurzaamheidsScore", 
                "Duurzaamheidsscore moet tussen 0 en 100 liggen");
        }
    }

    private ProductDTO convertToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getNaam(),
                product.getBeschrijving(),
                product.getPrijs(),
                product.getDuurzaamheidsScore(),
                product.getDuurzaamheidsCertificaat(),
                product.getVerkoper().getId()
        );
    }

    private Product convertToEntity(ProductDTO productDTO) {
        Product product = new Product();
        updateProductFromDTO(product, productDTO);
        return product;
    }

    private void updateProductFromDTO(Product product, ProductDTO productDTO) {
        product.setNaam(productDTO.getNaam());
        product.setBeschrijving(productDTO.getBeschrijving());
        product.setPrijs(productDTO.getPrijs());
        product.setDuurzaamheidsScore(productDTO.getDuurzaamheidsScore());
        product.setDuurzaamheidsCertificaat(productDTO.getDuurzaamheidsCertificaat());
        
        if (productDTO.getVerkoperId() != null) {
            User verkoper = userRepository.findById(productDTO.getVerkoperId())
                    .orElseThrow(() -> new UserNotFoundException(productDTO.getVerkoperId()));
            product.setVerkoper(verkoper);
        }
    }
}