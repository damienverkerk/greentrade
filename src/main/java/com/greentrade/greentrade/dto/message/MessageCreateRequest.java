package com.greentrade.greentrade.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequest {
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
}