package com.greentrade.greentrade.exception.security;

public class UserNotFoundException extends SecurityException {
    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }

    public UserNotFoundException(Long id) {
        super("User not found with ID: " + id);
    }
}