package com.project.management.service;

import com.project.management.dto.LoginRequestDTO;
import com.project.management.dto.LoginResponseDTO;
import com.project.management.dto.RegisterRequestDTO;
import com.project.management.Models.User;
import com.project.management.repository.UserRepository;
import com.project.management.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.sasl.AuthenticationException;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponseDTO login(LoginRequestDTO loginRequest) throws AuthenticationException {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(user);
        return new LoginResponseDTO(token, user.getUsername(), user.getRole(), user.getAssignedProjects());
    }

    public void register(RegisterRequestDTO registerRequest) {
        // Check if username or email is already taken
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        // Create a new user and set their details
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(registerRequest.getRole());  //

        // Save the user in the database
        userRepository.save(user);
    }
}
