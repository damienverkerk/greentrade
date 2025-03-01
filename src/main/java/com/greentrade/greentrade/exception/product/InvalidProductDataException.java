package com.greentrade.greentrade.exception.product;

public class InvalidProductDataException extends ProductException {
    public InvalidProductDataException(String field, String reason) {
        super("Invalid product data for field '" + field + "': " + reason);
    }
}