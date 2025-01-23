package com.greentrade.greentrade.exception.security;

public class UserNotFoundException extends SecurityException {
    public UserNotFoundException(String email) {
        super("Gebruiker niet gevonden met email: " + email);
    }

    public UserNotFoundException(Long id) {
        super("Gebruiker niet gevonden met ID: " + id);
    }
}