package com.greentrade.greentrade.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

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

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @InjectMocks
    private AuthenticationService authenticationService;
    
    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        validRegisterRequest = RegisterRequest.builder()
            .name("Test User")
            .email("test@example.com")
            .password("Test123!@#")
            .role(Role.ROLE_BUYER.toString())
            .build();
          
        validLoginRequest = LoginRequest.builder()
            .email("test@example.com")
            .password("Test123!@#")
            .build();
        
        testUser = User.builder()
            .id(1L)
            .name("Test User")
            .email("test@example.com")
            .password("encoded_password")
            .role(Role.ROLE_BUYER)
            .verificationStatus(true)
            .build();
    }

    @Test
    void register_WithValidData_CreatesUserAndReturnsToken() {
       
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("test.jwt.token");

      
        AuthResponse result = authenticationService.register(validRegisterRequest);

        
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals("test.jwt.token", result.getToken());
        assertEquals(Role.ROLE_BUYER.toString(), result.getRole());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ThrowsSecurityException() {
        
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        
        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
        assertEquals("Email is already in use", thrown.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithWeakPassword_ThrowsSecurityException() {
       
        validRegisterRequest.setPassword("weak");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        
        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
        assertTrue(thrown.getMessage().contains("Password must be at least 8 characters long"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithPasswordMissingLetter_ThrowsSecurityException() {
        
        validRegisterRequest.setPassword("12345678!");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        
        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
        assertEquals("Password must contain at least one letter, one number, and one special character", 
            thrown.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithPasswordMissingNumber_ThrowsSecurityException() {
        
        validRegisterRequest.setPassword("Password!");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        
        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
        assertEquals("Password must contain at least one letter, one number, and one special character", 
            thrown.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithPasswordMissingSpecialChar_ThrowsSecurityException() {
        
        validRegisterRequest.setPassword("Password123");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        
        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
        assertEquals("Password must contain at least one letter, one number, and one special character", 
            thrown.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_WithValidCredentials_ReturnsToken() {
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("test.jwt.token");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(testUser, null));

       
        AuthResponse result = authenticationService.authenticate(validLoginRequest);

        
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals("test.jwt.token", result.getToken());
        assertEquals(Role.ROLE_BUYER.toString(), result.getRole());
        assertEquals("test@example.com", result.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_WithInvalidCredentials_ThrowsInvalidCredentialsException() {
        
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        
        InvalidCredentialsException thrown = assertThrows(InvalidCredentialsException.class, () -> 
            authenticationService.authenticate(validLoginRequest)
        );
        assertEquals("Ongeldige inloggegevens", thrown.getMessage());
    }

    @Test
    void authenticate_WithDisabledAccount_ThrowsSecurityException() {
        
        when(authenticationManager.authenticate(any()))
            .thenThrow(new DisabledException("Account is disabled"));

        
        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.authenticate(validLoginRequest)
        );
        assertEquals("Account is gedeactiveerd", thrown.getMessage());
    }

    @Test
    void authenticate_WithNonExistentUser_ThrowsUserNotFoundException() {
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(testUser, null));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        
        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> 
            authenticationService.authenticate(validLoginRequest)
        );
        assertEquals("Gebruiker niet gevonden met email: test@example.com", thrown.getMessage());
    }
}