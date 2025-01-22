package com.greentrade.greentrade.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Email is verplicht")
    @Email(message = "Email moet geldig zijn")
    private String email;
    
    @NotBlank(message = "Wachtwoord is verplicht")
    private String wachtwoord;
}