package com.greentrade.greentrade.exception.verification;

public class VerificationNotFoundException extends ProductVerificationException {
    public VerificationNotFoundException(Long id) {
        super("Verification not found with ID: " + id);
    }
}