package com.greentrade.greentrade.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductDTO {
    private Long id;

    @NotBlank(message = "Naam is verplicht")
    @Size(min = 2, max = 100, message = "Naam moet tussen 2 en 100 tekens zijn")
    private String naam;

    @Size(max = 1000, message = "Beschrijving mag niet langer zijn dan 1000 tekens")
    private String beschrijving;

    @NotNull(message = "Prijs is verplicht")
    @DecimalMin(value = "0.0", inclusive = false, message = "Prijs moet groter zijn dan 0")
    private BigDecimal prijs;

    @Min(value = 0, message = "Duurzaamheidsscore moet minimaal 0 zijn")
    @Max(value = 100, message = "Duurzaamheidsscore mag maximaal 100 zijn")
    private Integer duurzaamheidsScore;

    private String duurzaamheidsCertificaat;

    @NotNull(message = "Verkoper ID is verplicht")
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