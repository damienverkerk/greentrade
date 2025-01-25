package com.greentrade.greentrade.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.greentrade.greentrade.dto.AuthRequest;
import com.greentrade.greentrade.dto.RegisterRequest;
import com.greentrade.greentrade.exception.security.SecurityException;
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
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registrerenMetBestaandeEmailGeeftFout() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
    }

    @Test
    void registrerenMetZwakWachtwoordGeeftFout() {
        validRegisterRequest.setWachtwoord("zwak");

        assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
    }

    @Test
    void registrerenMetLeegWachtwoordGeeftFout() {
        validRegisterRequest.setWachtwoord("");

        assertThrows(SecurityException.class, () -> 
            authenticationService.register(validRegisterRequest)
        );
    }

    @Test
    void loginMetGeldigeGegevensGelukt() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("test.jwt.token");

        var result = authenticationService.authenticate(validLoginRequest);

        assertNotNull(result);
        assertNotNull(result.getToken());
        verify(authenticationManager).authenticate(
            any(UsernamePasswordAuthenticationToken.class)
        );
    }

    @Test
    void loginMetOngeldigeGegevensGeeftFout() {
        when(authenticationManager.authenticate(any()))
            .thenThrow(new SecurityException("Ongeldige inloggegevens"));

        assertThrows(SecurityException.class, () -> 
            authenticationService.authenticate(validLoginRequest)
        );
    }
}