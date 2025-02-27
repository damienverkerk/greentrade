package com.greentrade.greentrade.mappers;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.product.ProductCreateRequest;
import com.greentrade.greentrade.dto.product.ProductResponse;
import com.greentrade.greentrade.dto.product.ProductUpdateRequest;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.User;

@Component
public class ProductMapper {
    
    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .sustainabilityScore(product.getSustainabilityScore())
                .sustainabilityCertificate(product.getSustainabilityCertificate())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .build();
    }
    
    public Product createRequestToEntity(ProductCreateRequest request, User seller) {
        if (request == null) {
            return null;
        }
        
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSustainabilityScore(request.getSustainabilityScore());
        product.setSustainabilityCertificate(request.getSustainabilityCertificate());
        product.setSeller(seller);
        
        return product;
    }
    
    public void updateEntityFromRequest(Product product, ProductUpdateRequest request, User seller) {
        if (request == null || product == null) {
            return;
        }
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSustainabilityScore(request.getSustainabilityScore());
        product.setSustainabilityCertificate(request.getSustainabilityCertificate());
        if (seller != null) {
            product.setSeller(seller);
        }
    }
}