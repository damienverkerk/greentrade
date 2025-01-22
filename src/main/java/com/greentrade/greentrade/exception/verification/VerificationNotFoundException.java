package com.greentrade.greentrade.exception.verification;

public class VerificationNotFoundException extends ProductVerificationException {
    public VerificationNotFoundException(Long id) {
        super("Verificatie niet gevonden met ID: " + id);
    }
}