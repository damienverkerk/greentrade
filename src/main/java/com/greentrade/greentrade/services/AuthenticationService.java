package com.greentrade.greentrade.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.AuthRequest;
import com.greentrade.greentrade.dto.AuthResponse;
import com.greentrade.greentrade.dto.RegisterRequest;
import com.greentrade.greentrade.models.Role;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.UserRepository;
import com.greentrade.greentrade.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is al in gebruik");
        }

        var user = User.builder()
                .naam(request.getNaam())
                .email(request.getEmail())
                .wachtwoord(passwordEncoder.encode(request.getWachtwoord()))
                .role(Role.valueOf(request.getRole()))
                .verificatieStatus(true) 
                .build();
        
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(savedUser);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            System.out.println("Authenticating user: " + request.getEmail());
            
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getWachtwoord()
                    )
            );
            
            System.out.println("Authentication successful");
            
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden"));
            
            System.out.println("User found, generating token");
            var jwtToken = jwtService.generateToken(user);
            System.out.println("Token generated");
            
            return AuthResponse.builder()
                    .token(jwtToken)
                    .build();
        } catch (Exception e) {
            System.out.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}