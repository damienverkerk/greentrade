package com.greentrade.greentrade.services;

import com.greentrade.greentrade.dto.ProductDTO;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.ProductRepository;
import com.greentrade.greentrade.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
                .orElse(null);
    }

    public List<ProductDTO> getProductsByVerkoper(Long verkoperId) {
        User verkoper = userRepository.findById(verkoperId)
                .orElseThrow(() -> new RuntimeException("Verkoper niet gevonden met id: " + verkoperId));
        return productRepository.findByVerkoper(verkoper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProductsByName(String naam) {
        return productRepository.findByNaamContainingIgnoreCase(naam).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByDuurzaamheidsScore(Integer minimumScore) {
        return productRepository.findByDuurzaamheidsScoreGreaterThanEqual(minimumScore).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setNaam(productDTO.getNaam());
                    product.setBeschrijving(productDTO.getBeschrijving());
                    product.setPrijs(productDTO.getPrijs());
                    product.setDuurzaamheidsScore(productDTO.getDuurzaamheidsScore());
                    product.setDuurzaamheidsCertificaat(productDTO.getDuurzaamheidsCertificaat());
                    return convertToDTO(productRepository.save(product));
                })
                .orElseThrow(() -> new RuntimeException("Product niet gevonden met id: " + id));
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product niet gevonden met id: " + id);
        }
        productRepository.deleteById(id);
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
        product.setNaam(productDTO.getNaam());
        product.setBeschrijving(productDTO.getBeschrijving());
        product.setPrijs(productDTO.getPrijs());
        product.setDuurzaamheidsScore(productDTO.getDuurzaamheidsScore());
        product.setDuurzaamheidsCertificaat(productDTO.getDuurzaamheidsCertificaat());
        
        if (productDTO.getVerkoperId() != null) {
            User verkoper = userRepository.findById(productDTO.getVerkoperId())
                    .orElseThrow(() -> new RuntimeException("Verkoper niet gevonden met id: " + productDTO.getVerkoperId()));
            product.setVerkoper(verkoper);
        }
        
        return product;
    }
}