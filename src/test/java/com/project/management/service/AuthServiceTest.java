package com.project.management.service;

import com.project.management.dto.LoginRequestDTO;
import com.project.management.dto.LoginResponseDTO;
import com.project.management.dto.RegisterRequestDTO;
import com.project.management.Models.User;
import com.project.management.Models.UserRole;
import com.project.management.exception.UserAlreadyExistsException;
import com.project.management.repository.UserRepository;
import com.project.management.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import javax.security.sasl.AuthenticationException;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthService authService;

    private User testUser;
    private LoginRequestDTO loginRequest;
    private RegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.USER);
        List<String> projects = new ArrayList<>();
        testUser.setAssignedProjects(projects);

        // Setup login request
        loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("password123");

        // Setup register request
        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newUser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("new@example.com");
        registerRequest.setRole(UserRole.USER);

        // Reset the mocks before each test
        reset(userRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void login_SuccessfulLogin() throws AuthenticationException {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn("testToken");

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertEquals("testUser", response.getUsername());
        assertEquals(UserRole.USER, response.getRole());
        verify(userRepository).findByUsername("testUser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider).generateToken(testUser);
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(AuthenticationException.class, () ->
                authService.login(loginRequest)
        );
        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByUsername("testUser");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_InvalidPassword() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(AuthenticationException.class, () ->
                authService.login(loginRequest)
        );
        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByUsername("testUser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void register_SuccessfulRegistration() {
        // Arrange
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // Act
        assertDoesNotThrow(() -> authService.register(registerRequest));

        // Assert
        verify(userRepository).existsByUsername("newUser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameTaken() {
        // Arrange
        when(userRepository.existsByUsername("newUser")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(UserAlreadyExistsException.class, () ->
                authService.register(registerRequest)
        );
        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository).existsByUsername("newUser");
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_EmailTaken() {
        // Arrange
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(UserAlreadyExistsException.class, () ->
                authService.register(registerRequest)
        );
        assertEquals("Email is already taken", exception.getMessage());
        verify(userRepository).existsByUsername("newUser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any());
    }
}