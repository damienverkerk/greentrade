package com.greentrade.greentrade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {
    private Long id;
    private Long koperId;
    private Long productId;
    private BigDecimal bedrag;
    private LocalDateTime datum;
    private String status;

    // Constructors
    public TransactionDTO() {}

    public TransactionDTO(Long id, Long koperId, Long productId, BigDecimal bedrag, 
                          LocalDateTime datum, String status) {
        this.id = id;
        this.koperId = koperId;
        this.productId = productId;
        this.bedrag = bedrag;
        this.datum = datum;
        this.status = status;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKoperId() {
        return koperId;
    }

    public void setKoperId(Long koperId) {
        this.koperId = koperId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getBedrag() {
        return bedrag;
    }

    public void setBedrag(BigDecimal bedrag) {
        this.bedrag = bedrag;
    }

    public LocalDateTime getDatum() {
        return datum;
    }

    public void setDatum(LocalDateTime datum) {
        this.datum = datum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}