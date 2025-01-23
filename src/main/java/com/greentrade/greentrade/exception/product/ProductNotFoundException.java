package com.greentrade.greentrade.exception.product;

public class ProductNotFoundException extends ProductException {
    public ProductNotFoundException(Long id) {
        super("Product niet gevonden met ID: " + id);
    }
}