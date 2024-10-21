package com.greentrade.greentrade.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "berichten")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "afzender_id", nullable = false)
    private User afzender;

    @ManyToOne
    @JoinColumn(name = "ontvanger_id", nullable = false)
    private User ontvanger;

    @Column(nullable = false)
    private String onderwerp;

    @Column(nullable = false, length = 2000)
    private String inhoud;

    @Column(nullable = false)
    private LocalDateTime datumTijd;

    @Column(nullable = false)
    private boolean gelezen;

    // Constructors
    public Message() {}

    public Message(User afzender, User ontvanger, String onderwerp, String inhoud, LocalDateTime datumTijd) {
        this.afzender = afzender;
        this.ontvanger = ontvanger;
        this.onderwerp = onderwerp;
        this.inhoud = inhoud;
        this.datumTijd = datumTijd;
        this.gelezen = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getAfzender() {
        return afzender;
    }

    public void setAfzender(User afzender) {
        this.afzender = afzender;
    }

    public User getOntvanger() {
        return ontvanger;
    }

    public void setOntvanger(User ontvanger) {
        this.ontvanger = ontvanger;
    }

    public String getOnderwerp() {
        return onderwerp;
    }

    public void setOnderwerp(String onderwerp) {
        this.onderwerp = onderwerp;
    }

    public String getInhoud() {
        return inhoud;
    }

    public void setInhoud(String inhoud) {
        this.inhoud = inhoud;
    }

    public LocalDateTime getDatumTijd() {
        return datumTijd;
    }

    public void setDatumTijd(LocalDateTime datumTijd) {
        this.datumTijd = datumTijd;
    }

    public boolean isGelezen() {
        return gelezen;
    }

    public void setGelezen(boolean gelezen) {
        this.gelezen = gelezen;
    }
}