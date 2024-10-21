package com.greentrade.greentrade.dto;

import java.math.BigDecimal;

public class ProductDTO {
    private Long id;
    private String naam;
    private String beschrijving;
    private BigDecimal prijs;
    private Integer duurzaamheidsScore;
    private String duurzaamheidsCertificaat;
    private Long verkoperId;

    // Constructors
    public ProductDTO() {}

    public ProductDTO(Long id, String naam, String beschrijving, BigDecimal prijs, 
                      Integer duurzaamheidsScore, String duurzaamheidsCertificaat, Long verkoperId) {
        this.id = id;
        this.naam = naam;
        this.beschrijving = beschrijving;
        this.prijs = prijs;
        this.duurzaamheidsScore = duurzaamheidsScore;
        this.duurzaamheidsCertificaat = duurzaamheidsCertificaat;
        this.verkoperId = verkoperId;
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

    public String getBeschrijving() {
        return beschrijving;
    }

    public void setBeschrijving(String beschrijving) {
        this.beschrijving = beschrijving;
    }

    public BigDecimal getPrijs() {
        return prijs;
    }

    public void setPrijs(BigDecimal prijs) {
        this.prijs = prijs;
    }

    public Integer getDuurzaamheidsScore() {
        return duurzaamheidsScore;
    }

    public void setDuurzaamheidsScore(Integer duurzaamheidsScore) {
        this.duurzaamheidsScore = duurzaamheidsScore;
    }

    public String getDuurzaamheidsCertificaat() {
        return duurzaamheidsCertificaat;
    }

    public void setDuurzaamheidsCertificaat(String duurzaamheidsCertificaat) {
        this.duurzaamheidsCertificaat = duurzaamheidsCertificaat;
    }

    public Long getVerkoperId() {
        return verkoperId;
    }

    public void setVerkoperId(Long verkoperId) {
        this.verkoperId = verkoperId;
    }
}