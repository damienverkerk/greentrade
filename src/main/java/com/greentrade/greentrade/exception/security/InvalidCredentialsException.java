package com.greentrade.greentrade.exception.security;

public class InvalidCredentialsException extends SecurityException {
    public InvalidCredentialsException() {
        super("Ongeldige inloggegevens");
    }
}