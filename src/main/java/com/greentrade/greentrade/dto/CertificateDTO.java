package com.greentrade.greentrade.dto;

import java.time.LocalDate;

public class CertificateDTO {
    private Long id;
    private String name;
    private String issuer;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String description;
    private String filePath;
    private Long userId;

    // Constructors
    public CertificateDTO() {}

    public CertificateDTO(Long id, String name, String issuer, LocalDate issueDate, 
                          LocalDate expiryDate, String description, String filePath, Long userId) {
        this.id = id;
        this.name = name;
        this.issuer = issuer;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.description = description;
        this.filePath = filePath;
        this.userId = userId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}