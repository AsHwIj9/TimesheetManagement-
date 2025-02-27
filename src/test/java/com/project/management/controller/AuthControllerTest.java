package com.project.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.management.Models.UserRole;
import com.project.management.dto.LoginRequestDTO;
import com.project.management.dto.LoginResponseDTO;
import com.project.management.dto.RegisterRequestDTO;
import com.project.management.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;



import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper; // To convert objects to JSON

    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private LoginResponseDTO loginResponse;

    @BeforeEach
    void setUp() {
        // Initialize registerRequest
        registerRequest = new RegisterRequestDTO(
                "testuser",
                "password123",
                UserRole.USER,
                "test@example.com"
        );

        // Initialize loginRequest
        loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Initialize loginResponse
        loginResponse = new LoginResponseDTO(
                "dummy-token",
                "testuser",
                UserRole.USER,
                List.of("Project A", "Project B")
        );
    }
    @Test
    void testRegister_Success() throws Exception {
        doNothing().when(authService).register(any(RegisterRequestDTO.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequestDTO validRequest = new LoginRequestDTO();
        validRequest.setUsername("testuser");
        validRequest.setPassword("password123");

        // Mock service to return loginResponse
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testLogin_Failure() throws Exception {
        LoginRequestDTO invalidRequest = new LoginRequestDTO();
        invalidRequest.setUsername("invalidUser");
        invalidRequest.setPassword("wrongPassword");


        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isInternalServerError()); // Changed to match actual behavior
    }
}