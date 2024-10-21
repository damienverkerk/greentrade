package com.greentrade.greentrade.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificaten")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naam;

    @Column(nullable = false)
    private String uitgever;

    @Column(name = "uitgiftedatum", nullable = false)
    private LocalDate uitgifteDatum;

    @Column(name = "vervaldatum")
    private LocalDate vervaldatum;

    @Column(length = 1000)
    private String beschrijving;

    @Column(name = "bestandspad")
    private String bestandsPad;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Constructors
    public Certificate() {}

    public Certificate(String naam, String uitgever, LocalDate uitgifteDatum, LocalDate vervaldatum, String beschrijving, String bestandsPad, User user) {
        this.naam = naam;
        this.uitgever = uitgever;
        this.uitgifteDatum = uitgifteDatum;
        this.vervaldatum = vervaldatum;
        this.beschrijving = beschrijving;
        this.bestandsPad = bestandsPad;
        this.user = user;
    }

    // Getters and Setters
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

    public String getUitgever() {
        return uitgever;
    }

    public void setUitgever(String uitgever) {
        this.uitgever = uitgever;
    }

    public LocalDate getUitgifteDatum() {
        return uitgifteDatum;
    }

    public void setUitgifteDatum(LocalDate uitgifteDatum) {
        this.uitgifteDatum = uitgifteDatum;
    }

    public LocalDate getVervaldatum() {
        return vervaldatum;
    }

    public void setVervaldatum(LocalDate vervaldatum) {
        this.vervaldatum = vervaldatum;
    }

    public String getBeschrijving() {
        return beschrijving;
    }

    public void setBeschrijving(String beschrijving) {
        this.beschrijving = beschrijving;
    }

    public String getBestandsPad() {
        return bestandsPad;
    }

    public void setBestandsPad(String bestandsPad) {
        this.bestandsPad = bestandsPad;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}