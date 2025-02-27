package com.greentrade.greentrade.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
             message = "Password must be at least 8 characters long and contain at least one letter, one number, and one special character")
    private String password;
            
    @Pattern(regexp = "ROLE_BUYER|ROLE_SELLER|ROLE_ADMIN", message = "Role must be one of: ROLE_BUYER, ROLE_SELLER, ROLE_ADMIN")
    @Builder.Default
    private String role = "ROLE_BUYER";
}