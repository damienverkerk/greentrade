package com.greentrade.greentrade.dto;

import java.time.LocalDate;

public class CertificateDTO {
    private Long id;
    private String naam;
    private String uitgever;
    private LocalDate uitgifteDatum;
    private LocalDate vervaldatum;
    private String beschrijving;
    private String bestandsPad;
    private Long userId;

    // Constructors
    public CertificateDTO() {}

    public CertificateDTO(Long id, String naam, String uitgever, LocalDate uitgifteDatum, 
                          LocalDate vervaldatum, String beschrijving, String bestandsPad, Long userId) {
        this.id = id;
        this.naam = naam;
        this.uitgever = uitgever;
        this.uitgifteDatum = uitgifteDatum;
        this.vervaldatum = vervaldatum;
        this.beschrijving = beschrijving;
        this.bestandsPad = bestandsPad;
        this.userId = userId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}