package com.greentrade.greentrade.exception.verification;

public class DuplicateVerificationException extends ProductVerificationException {
    public DuplicateVerificationException(Long productId) {
        super("Er is al een lopende verificatie voor product met ID: " + productId);
    }
}