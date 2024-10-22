package com.greentrade.greentrade.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDTO implements Serializable {
    private Long id;

    @NotBlank(message = "Naam is verplicht")
    @Size(min = 2, max = 50, message = "Naam moet tussen 2 en 50 tekens zijn")
    private String naam;

    @NotBlank(message = "Email is verplicht")
    @Email(message = "Email moet een geldig email adres zijn")
    private String email;

    @NotBlank(message = "Rol is verplicht")
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