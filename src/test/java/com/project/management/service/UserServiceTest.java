package com.project.management.service;

import com.project.management.dto.UserDTO;
import com.project.management.dto.UserRegistrationDTO;
import com.project.management.dto.UserWeeklyStatsDTO;
import com.project.management.Models.Timesheet;
import com.project.management.Models.User;
import com.project.management.Models.UserRole;
import com.project.management.exception.ResourceNotFoundException;
import com.project.management.exception.UserAlreadyExistsException;
import com.project.management.repository.TimesheetRepository;
import com.project.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.ValidationException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TimesheetRepository timesheetRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationDTO registrationDTO;
    private Timesheet testTimesheet;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId("test-id");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded-password");
        testUser.setRole(UserRole.USER);
        testUser.setAssignedProjects(List.of("project1", "project2"));
        testUser.setCreatedAt(LocalDateTime.now());

        // Setup registration DTO
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("newuser");
        registrationDTO.setEmail("new@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setConfirmPassword("password123");

        // Setup test timesheet with EnumMap
        testTimesheet = new Timesheet();
        testTimesheet.setId("timesheet-id");
        testTimesheet.setUserId("test-id");
        testTimesheet.setProjectId("project1");
        testTimesheet.setWeekStartDate(LocalDate.now());

        EnumMap<DayOfWeek, Integer> dailyHours = new EnumMap<>(DayOfWeek.class);
        dailyHours.put(DayOfWeek.MONDAY, 8);
        dailyHours.put(DayOfWeek.TUESDAY, 8);
        testTimesheet.setDailyHours(dailyHours);
    }

    @Test
    void createUser_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.createUser(registrationDTO);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () ->
                userService.createUser(registrationDTO)
        );
    }

    @Test
    void createUser_PasswordMismatch_ThrowsException() {
        registrationDTO.setConfirmPassword("different-password");

        assertThrows(ValidationException.class, () ->
                userService.createUser(registrationDTO)
        );
    }

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.getFirst().getUsername());
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById("test-id")).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserById("test-id");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                userService.getUserById("non-existent-id")
        );
    }

    @Test
    void deleteUserById_Success() {
        when(userRepository.findById("test-id")).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        assertDoesNotThrow(() -> userService.deleteUserById("test-id"));
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUserById_NotFound_ThrowsException() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.deleteUserById("non-existent-id")
        );
    }

    @Test
    void getUsersWeeklyStats_Success() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(1);
        List<User> users = List.of(testUser);
        List<Timesheet> timesheets = List.of(testTimesheet);

        when(userRepository.findAll()).thenReturn(users);
        when(timesheetRepository.findByUserIdAndWeekStartDateBetween(
                anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(timesheets);

        List<UserWeeklyStatsDTO> result = userService.getUsersWeeklyStats(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        UserWeeklyStatsDTO stats = result.getFirst();
        assertEquals("test-id", stats.getUserId());
        assertEquals("testuser", stats.getUsername());
        assertEquals(16, stats.getTotalHours()); // 8 hours for Monday + 8 for Tuesday
        assertTrue(stats.getProjectHours().containsKey("project1"));
    }
}