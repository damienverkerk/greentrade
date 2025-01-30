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

import com.greentrade.greentrade.dto.AuthRequest;
import com.greentrade.greentrade.dto.RegisterRequest;
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
    private AuthRequest validLoginRequest;
    private User testUser;
    
    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        validRegisterRequest = RegisterRequest.builder()
            .naam("Test User")
            .email("test@example.com")
            .wachtwoord("Test123!@#")
            .role(Role.ROLE_KOPER.toString())
            .build();
            
        validLoginRequest = AuthRequest.builder()
            .email("test@example.com")
            .wachtwoord("Test123!@#")
            .build();
            
        testUser = User.builder()
            .id(1L)
            .naam("Test User")
            .email("test@example.com")
            .wachtwoord("encoded_password")
            .role(Role.ROLE_KOPER)
            .verificatieStatus(true)
            .build();
    }

    @Test
    void registrerenMetGeldigeGegevensGelukt() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("test.jwt.token");

        var result = authenticationService.register(validRegisterRequest);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals("test.jwt.token", result.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registrerenMetBestaandeEmailGeeftFout() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
        assertEquals("Email is al in gebruik", thrown.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registrerenMetZwakWachtwoordGeeftFout() {
        validRegisterRequest.setWachtwoord("zwak");

        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
        assertTrue(thrown.getMessage().contains("Wachtwoord moet minimaal 8 karakters lang zijn"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registrerenMetLeegWachtwoordGeeftFout() {
        validRegisterRequest.setWachtwoord("");

        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
        assertTrue(thrown.getMessage().contains("Wachtwoord moet minimaal 8 karakters lang zijn"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registrerenZonderLetterInWachtwoordGeeftFout() {
        validRegisterRequest.setWachtwoord("12345678!");

        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
        assertEquals("Wachtwoord moet minimaal één letter, één cijfer en één speciaal karakter bevatten", 
            thrown.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registrerenZonderCijferInWachtwoordGeeftFout() {
        validRegisterRequest.setWachtwoord("Password!");

        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
        assertEquals("Wachtwoord moet minimaal één letter, één cijfer en één speciaal karakter bevatten", 
            thrown.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registrerenZonderSpeciaalTekenInWachtwoordGeeftFout() {
        validRegisterRequest.setWachtwoord("Password123");

        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
        assertEquals("Wachtwoord moet minimaal één letter, één cijfer en één speciaal karakter bevatten", 
            thrown.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginMetGeldigeGegevensGelukt() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("test.jwt.token");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(testUser, null));

        var result = authenticationService.authenticate(validLoginRequest);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals("test.jwt.token", result.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void loginMetOngeldigeGegevensGeeftFout() {
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        InvalidCredentialsException thrown = assertThrows(InvalidCredentialsException.class, () -> 
            authenticationService.authenticate(validLoginRequest)
        );
        assertNotNull(thrown);
    }

    @Test
    void loginMetGedeactiveerdAccountGeeftFout() {
        when(authenticationManager.authenticate(any()))
            .thenThrow(new DisabledException("Account is disabled"));

        SecurityException thrown = assertThrows(SecurityException.class, () -> 
            authenticationService.authenticate(validLoginRequest)
        );
        assertEquals("Account is gedeactiveerd", thrown.getMessage());
    }

    @Test
    void loginMetOnbekendeGebruikerGeeftFout() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(testUser, null));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> 
            authenticationService.authenticate(validLoginRequest)
        );
        assertNotNull(thrown);
        assertEquals("Gebruiker niet gevonden met email: test@example.com", thrown.getMessage());
    }
}