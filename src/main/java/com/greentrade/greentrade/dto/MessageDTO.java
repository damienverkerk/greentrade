package com.greentrade.greentrade.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MessageDTO {
    private Long id;

    @NotNull(message = "Afzender ID is verplicht")
    private Long afzenderId;

    @NotNull(message = "Ontvanger ID is verplicht")
    private Long ontvangerId;

    @NotBlank(message = "Onderwerp is verplicht")
    @Size(max = 100, message = "Onderwerp mag niet langer zijn dan 100 tekens")
    private String onderwerp;

    @NotBlank(message = "Inhoud is verplicht")
    @Size(max = 2000, message = "Inhoud mag niet langer zijn dan 2000 tekens")
    private String inhoud;

    private LocalDateTime datumTijd;

    private boolean gelezen;

    // Constructors
    public MessageDTO() {
    }

    public MessageDTO(Long id, Long afzenderId, Long ontvangerId, String onderwerp, 
                     String inhoud, LocalDateTime datumTijd, boolean gelezen) {
        this.id = id;
        this.afzenderId = afzenderId;
        this.ontvangerId = ontvangerId;
        this.onderwerp = onderwerp;
        this.inhoud = inhoud;
        this.datumTijd = datumTijd;
        this.gelezen = gelezen;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAfzenderId() {
        return afzenderId;
    }

    public void setAfzenderId(Long afzenderId) {
        this.afzenderId = afzenderId;
    }

    public Long getOntvangerId() {
        return ontvangerId;
    }

    public void setOntvangerId(Long ontvangerId) {
        this.ontvangerId = ontvangerId;
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