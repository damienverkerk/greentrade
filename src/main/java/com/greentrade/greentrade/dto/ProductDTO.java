package com.greentrade.greentrade.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot be longer than 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Sustainability score must be at least 0")
    @Max(value = 100, message = "Sustainability score cannot be more than 100")
    private Integer sustainabilityScore;

    private String sustainabilityCertificate;

    @NotNull(message = "Seller ID is required")
    private Long sellerId;

    // Constructors
    public ProductDTO() {}

    public ProductDTO(Long id, String name, String description, BigDecimal price, 
                      Integer sustainabilityScore, String sustainabilityCertificate, Long sellerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.sustainabilityScore = sustainabilityScore;
        this.sustainabilityCertificate = sustainabilityCertificate;
        this.sellerId = sellerId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getSustainabilityScore() {
        return sustainabilityScore;
    }

    public void setSustainabilityScore(Integer sustainabilityScore) {
        this.sustainabilityScore = sustainabilityScore;
    }

    public String getSustainabilityCertificate() {
        return sustainabilityCertificate;
    }

    public void setSustainabilityCertificate(String sustainabilityCertificate) {
        this.sustainabilityCertificate = sustainabilityCertificate;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
}