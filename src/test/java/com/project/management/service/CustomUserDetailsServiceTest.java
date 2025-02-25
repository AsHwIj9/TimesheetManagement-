package com.project.management.service;

import com.project.management.Models.User;
import com.project.management.Models.UserRole;
import com.project.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-id");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded-password");
        testUser.setRole(UserRole.USER);
        testUser.setAssignedProjects(List.of("project1", "project2"));
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void loadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isCredentialsNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("nonexistent")
        );
        assertEquals("User not found with username: nonexistent", exception.getMessage());
    }

    @Test
    void loadUserById_Success() {
        // Arrange
        when(userRepository.findById("test-id")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserById("test-id");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isCredentialsNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserById_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserById("nonexistent-id")
        );
        assertEquals("User not found with ID: nonexistent-id", exception.getMessage());
    }

    @Test
    void verifyUserDetailsImplementation() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        // Verify all UserDetails interface methods
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertNotNull(userDetails.getAuthorities());
        assertNotNull(userDetails.getPassword());
        assertNotNull(userDetails.getUsername());
    }
}