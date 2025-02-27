package com.greentrade.greentrade.controllers;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.AuthResponse;
import com.greentrade.greentrade.dto.RegisterRequest;
import com.greentrade.greentrade.models.Role;
import com.greentrade.greentrade.services.AuthenticationService;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void whenRegisterUser_thenReturn200() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
            .name("Test User")
            .email("test@example.com")
            .password("Test123!@#")
            .role(Role.ROLE_BUYER.toString())
            .build();

        AuthResponse mockResponse = AuthResponse.builder()
            .token("mock-jwt-token")
            .build();

        when(authenticationService.register(any(RegisterRequest.class)))
            .thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }
    
    @Test
    void whenRegisterUserWithInvalidData_thenReturn400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
            .name("")
            .email("invalid-email")
            .password("")
            .role(Role.ROLE_BUYER.toString())
            .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}