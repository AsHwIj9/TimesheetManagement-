package com.project.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.management.Models.UserRole;
import com.project.management.dto.UserDTO;
import com.project.management.dto.UserRegistrationDTO;
import com.project.management.dto.UserWeeklyStatsDTO;
import com.project.management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO testUser;
    private UserRegistrationDTO registrationDTO;
    private List<UserDTO> userList;
    private List<UserWeeklyStatsDTO> weeklyStats;

    @BeforeEach
    void setUp() {
        // Create registration DTO
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setConfirmPassword("password123"); // Add this line


        // Create test user
        testUser = new UserDTO();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(UserRole.USER);
        testUser.setAssignedProjects(Arrays.asList("project1", "project2"));
        testUser.setCreatedAt(LocalDateTime.now());

        // Create test user list
        UserDTO user2 = new UserDTO();
        user2.setId("user456");
        user2.setUsername("anotheruser");
        user2.setEmail("another@example.com");
        user2.setPassword("password456");
        user2.setRole(UserRole.USER);
        user2.setAssignedProjects(Arrays.asList("project3"));
        user2.setCreatedAt(LocalDateTime.now());

        userList = Arrays.asList(testUser, user2);

        // Create weekly stats
        Map<String, Integer> projectHours1 = new HashMap<>();
        projectHours1.put("project1", 20);
        projectHours1.put("project2", 15);

        Map<String, Integer> projectHours2 = new HashMap<>();
        projectHours2.put("project3", 30);

        UserWeeklyStatsDTO stats1 = new UserWeeklyStatsDTO();
        stats1.setUserId("user123");
        stats1.setUsername("testuser");
        stats1.setWeekStartDate(LocalDate.now().minusDays(7));
        stats1.setProjectHours(projectHours1);
        stats1.setTotalHours(35);
        stats1.setUtilizationPercentage(87.5);

        UserWeeklyStatsDTO stats2 = new UserWeeklyStatsDTO();
        stats2.setUserId("user456");
        stats2.setUsername("anotheruser");
        stats2.setWeekStartDate(LocalDate.now().minusDays(7));
        stats2.setProjectHours(projectHours2);
        stats2.setTotalHours(30);
        stats2.setUtilizationPercentage(75.0);

        weeklyStats = Arrays.asList(stats1, stats2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateUser() throws Exception {
        // Given
        when(userService.createUser(any())).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User created successfully")))
                .andExpect(jsonPath("$.data.id", is("user123")))
                .andExpect(jsonPath("$.data.username", is("testuser")));

        verify(userService).createUser(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllUsers() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(userList);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("user123")))
                .andExpect(jsonPath("$[1].id", is("user456")));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserById() throws Exception {
        // Given
        when(userService.getUserById("user123")).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("user123")))
                .andExpect(jsonPath("$.username", is("testuser")));

        verify(userService).getUserById("user123");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUsersWeeklyStats() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        when(userService.getUsersWeeklyStats(eq(startDate), eq(endDate))).thenReturn(weeklyStats);

        // When & Then
        mockMvc.perform(get("/api/users/stats/weekly")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId", is("user123")))
                .andExpect(jsonPath("$[1].userId", is("user456")));

        verify(userService).getUsersWeeklyStats(eq(startDate), eq(endDate));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteUser() throws Exception {
        // Given
        doNothing().when(userService).deleteUserById("user123");

        // When & Then
        mockMvc.perform(delete("/api/users/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User deleted successfully")))
                .andExpect(jsonPath("$.data", is("user123")));

        verify(userService).deleteUserById("user123");
    }
}