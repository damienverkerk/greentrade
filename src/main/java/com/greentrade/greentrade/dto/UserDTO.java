package com.greentrade.greentrade.dto;

import java.io.Serializable;

public class UserDTO implements Serializable {
    private Long id;
    private String naam;
    private String email;
    private String role;

    // Constructors
    public UserDTO() {}

    public UserDTO(Long id, String naam, String email, String role) {
        this.id = id;
        this.naam = naam;
        this.email = email;
        this.role = role;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}