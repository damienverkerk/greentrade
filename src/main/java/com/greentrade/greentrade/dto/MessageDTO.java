package com.greentrade.greentrade.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MessageDTO {
    private Long id;

    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject cannot be longer than 100 characters")
    private String subject;

    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content cannot be longer than 2000 characters")
    private String content;

    private LocalDateTime timestamp;

    private boolean read;

    // Constructors
    public MessageDTO() {
    }

    public MessageDTO(Long id, Long senderId, Long receiverId, String subject, 
                     String content, LocalDateTime timestamp, boolean read) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.subject = subject;
        this.content = content;
        this.timestamp = timestamp;
        this.read = read;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}