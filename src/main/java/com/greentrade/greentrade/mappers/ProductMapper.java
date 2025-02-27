package com.greentrade.greentrade.mappers;

import com.greentrade.greentrade.dto.ProductDTO;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.User;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    
    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }
        
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSustainabilityScore(),
                product.getSustainabilityCertificate(),
                product.getSeller() != null ? product.getSeller().getId() : null
        );
    }
    
    public Product toEntity(ProductDTO dto, User seller) {
        if (dto == null) {
            return null;
        }
        
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setSustainabilityScore(dto.getSustainabilityScore());
        product.setSustainabilityCertificate(dto.getSustainabilityCertificate());
        product.setSeller(seller);
        
        return product;
    }
}