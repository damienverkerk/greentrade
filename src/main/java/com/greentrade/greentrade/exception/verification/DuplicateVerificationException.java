package com.greentrade.greentrade.exception.verification;

public class DuplicateVerificationException extends ProductVerificationException {
    public DuplicateVerificationException(Long productId) {
        super("An active verification already exists for product with ID: " + productId);
    }
}