package com.greentrade.greentrade.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greentrade.greentrade.dto.AuthRequest;
import com.greentrade.greentrade.dto.AuthResponse;
import com.greentrade.greentrade.dto.RegisterRequest;
import com.greentrade.greentrade.services.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authenticatie", description = "API endpoints voor registratie en inloggen")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Registreer een nieuwe gebruiker")
    @ApiResponse(responseCode = "200", description = "Registratie succesvol")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(summary = "Log in als bestaande gebruiker")
    @ApiResponse(responseCode = "200", description = "Inloggen succesvol")
    @PostMapping("/login")
public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
    try {
        System.out.println("Login poging voor gebruiker: " + request.getEmail());
        AuthResponse response = authenticationService.authenticate(request);
        System.out.println("Login succesvol, token gegenereerd");
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        System.out.println("Login fout: " + e.getMessage());
        throw e;
        }
    }
}