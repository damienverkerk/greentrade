package com.greentrade.greentrade.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Naam is verplicht")
    private String naam;
    
    @NotBlank(message = "Email is verplicht")
    @Email(message = "Email moet geldig zijn")
    private String email;
    
    @NotBlank(message = "Wachtwoord is verplicht")
    private String wachtwoord;
    
    @Builder.Default
    private String role = "ROLE_KOPER";
}