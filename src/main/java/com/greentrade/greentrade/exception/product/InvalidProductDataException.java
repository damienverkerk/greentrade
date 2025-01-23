package com.greentrade.greentrade.exception.product;

public class InvalidProductDataException extends ProductException {
    public InvalidProductDataException(String field, String reason) {
        super("Ongeldig product data voor veld '" + field + "': " + reason);
    }
}