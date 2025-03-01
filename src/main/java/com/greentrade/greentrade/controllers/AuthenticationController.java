package com.greentrade.greentrade.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greentrade.greentrade.dto.auth.AuthResponse;
import com.greentrade.greentrade.dto.auth.LoginRequest;
import com.greentrade.greentrade.dto.auth.RegisterRequest;
import com.greentrade.greentrade.services.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API endpoints for registration and login")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "200", description = "Registration successful")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Parameter(description = "Registration details", required = true)
            @Valid @RequestBody RegisterRequest request
    ) {
        // For auth endpoints, we typically return 200 OK with the token rather than 201 Created
        // since we're not truly creating a resource with a URI
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(summary = "Log in as existing user")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody LoginRequest request) {
        try {
            System.out.println("Login attempt for user: " + request.getEmail());
            AuthResponse response = authenticationService.authenticate(request);
            System.out.println("Login successful, token generated");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            throw e;
        }
    }
}