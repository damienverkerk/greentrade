package com.greentrade.greentrade.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String naam;

    @Column(unique = true)
    private String email;

    private String wachtwoord;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean verificatieStatus;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Certificate certificate;

    @OneToMany(mappedBy = "verkoper", cascade = CascadeType.ALL)
    private List<Product> producten = new ArrayList<>();

    @OneToMany(mappedBy = "afzender", cascade = CascadeType.ALL)
    private List<Message> verzondenBerichten = new ArrayList<>();

    @OneToMany(mappedBy = "ontvanger", cascade = CascadeType.ALL)
    private List<Message> ontvangenBerichten = new ArrayList<>();

    // Constructors
    public User() {}

    public User(String naam, String email, String wachtwoord, Role role, boolean verificatieStatus) {
        this.naam = naam;
        this.email = email;
        this.wachtwoord = wachtwoord;
        this.role = role;
        this.verificatieStatus = verificatieStatus;
    }

    // Getters en Setters
    public Long getId() {
        return id;
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

    public String getWachtwoord() {
        return wachtwoord;
    }

    public void setWachtwoord(String wachtwoord) {
        this.wachtwoord = wachtwoord;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isVerificatieStatus() {
        return verificatieStatus;
    }

    public void setVerificatieStatus(boolean verificatieStatus) {
        this.verificatieStatus = verificatieStatus;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public List<Product> getProducten() {
        return producten;
    }

    public void setProducten(List<Product> producten) {
        this.producten = producten;
    }

    public List<Message> getVerzondenBerichten() {
        return verzondenBerichten;
    }

    public void setVerzondenBerichten(List<Message> verzondenBerichten) {
        this.verzondenBerichten = verzondenBerichten;
    }

    public List<Message> getOntvangenBerichten() {
        return ontvangenBerichten;
    }

    public void setOntvangenBerichten(List<Message> ontvangenBerichten) {
        this.ontvangenBerichten = ontvangenBerichten;
    }
}