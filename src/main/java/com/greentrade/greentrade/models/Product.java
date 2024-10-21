package com.greentrade.greentrade.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "producten")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naam;

    @Column(length = 1000)
    private String beschrijving;

    @Column(nullable = false)
    private BigDecimal prijs;

    @Column(name = "duurzaamheids_score")
    private Integer duurzaamheidsScore;

    @Column(name = "duurzaamheids_certificaat")
    private String duurzaamheidsCertificaat;

    @ManyToOne
    @JoinColumn(name = "verkoper_id", nullable = false)
    private User verkoper;


    // Constructors
    public Product() {}

    public Product(String naam, String beschrijving, BigDecimal prijs, Integer duurzaamheidsScore, String duurzaamheidsCertificaat, User verkoper) {
        this.naam = naam;
        this.beschrijving = beschrijving;
        this.prijs = prijs;
        this.duurzaamheidsScore = duurzaamheidsScore;
        this.duurzaamheidsCertificaat = duurzaamheidsCertificaat;
        this.verkoper = verkoper;
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

    public User getVerkoper() {
        return verkoper;
    }

    public void setVerkoper(User verkoper) {
        this.verkoper = verkoper;
    }
}