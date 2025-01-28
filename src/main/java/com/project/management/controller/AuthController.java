package com.project.management.controller;

import com.project.management.dto.LoginRequestDTO;
import com.project.management.dto.LoginResponseDTO;
import com.project.management.dto.RegisterRequestDTO;
import com.project.management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.sasl.AuthenticationException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        log.info("Registering user with details: {}", registerRequest);
        authService.register(registerRequest);
        log.info("User registered successfully.");
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest)
            throws AuthenticationException {
        log.info("Login attempt with username: {}", loginRequest.getUsername());
        LoginResponseDTO response = authService.login(loginRequest);
        log.info("Login successful for username: {}", loginRequest.getUsername());
        return ResponseEntity.ok(response);
    }
}

