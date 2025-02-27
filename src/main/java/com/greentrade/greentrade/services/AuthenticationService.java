package com.greentrade.greentrade.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.auth.AuthResponse;
import com.greentrade.greentrade.dto.auth.LoginRequest;
import com.greentrade.greentrade.dto.auth.RegisterRequest;
import com.greentrade.greentrade.exception.security.InvalidCredentialsException;
import com.greentrade.greentrade.exception.security.SecurityException;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
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
        validateEmailAvailability(request.getEmail());
        validatePassword(request.getPassword());

        User user = createUserFromRequest(request);
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(savedUser);
        
        return createAuthResponse(jwtToken, savedUser);
    }

    public AuthResponse authenticate(LoginRequest request) {
        try {
            authenticateUser(request.getEmail(), request.getPassword());
            
            var user = findUserByEmail(request.getEmail());
            var jwtToken = jwtService.generateToken(user);
            
            return createAuthResponse(jwtToken, user);
        } catch (DisabledException e) {
            throw new SecurityException("Account is disabled");
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }
    }
    
    private void validateEmailAvailability(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new SecurityException("Email is already in use");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new SecurityException("Password must be at least 8 characters long");
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        
        if (!hasLetter || !hasDigit || !hasSpecial) {
            throw new SecurityException(
                "Password must contain at least one letter, one number, and one special character"
            );
        }
    }
    
    private User createUserFromRequest(RegisterRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole()))
                .verificationStatus(true)  // Default to true
                .build();
    }
    
    private void authenticateUser(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );
    }
    
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
    
    private AuthResponse createAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().toString())
                .email(user.getEmail())
                .build();
    }
}