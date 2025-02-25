package com.project.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.management.Models.User;
import com.project.management.Models.UserRole;
import com.project.management.dto.ApiResponse;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
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

    private UserDTO testUserDTO;
    private UserRegistrationDTO testUserRegistrationDTO;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO();
        testUserDTO.setId("1");
        testUserDTO.setUsername("testUser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setPassword("password123");
        testUserDTO.setRole(UserRole.USER);
        testUserDTO.setAssignedProjects(Arrays.asList("project1", "project2"));
        testUserDTO.setCreatedAt(LocalDateTime.now());

        testUserRegistrationDTO = new UserRegistrationDTO();
        testUserRegistrationDTO.setUsername("newUser");
        testUserRegistrationDTO.setEmail("newuser@example.com");
        testUserRegistrationDTO.setPassword("newpassword123");
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createUser_Success() throws Exception {
        when(userService.createUser(any(UserRegistrationDTO.class))).thenReturn(testUserDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRegistrationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.username").value(testUserDTO.getUsername()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAllUsers_Success() throws Exception {
        List<UserDTO> users = Arrays.asList(testUserDTO);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(testUserDTO.getUsername()))
                .andExpect(jsonPath("$[0].email").value(testUserDTO.getEmail()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getUserById_Success() throws Exception {
        when(userService.getUserById("1")).thenReturn(testUserDTO);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUserDTO.getUsername()))
                .andExpect(jsonPath("$.email").value(testUserDTO.getEmail()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getUsersWeeklyStats_Success() throws Exception {
        LocalDate startDate = LocalDate.now().minusWeeks(1);
        LocalDate endDate = LocalDate.now();

        UserWeeklyStatsDTO statsDTO = new UserWeeklyStatsDTO();
        List<UserWeeklyStatsDTO> statsList = Arrays.asList(statsDTO);

        when(userService.getUsersWeeklyStats(startDate, endDate)).thenReturn(statsList);

        mockMvc.perform(get("/api/users/stats/weekly")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"))
                .andExpect(jsonPath("$.data").value("1"));

        verify(userService).deleteUserById("1");
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void createUser_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRegistrationDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_ValidationFailure() throws Exception {
        UserRegistrationDTO invalidUser = new UserRegistrationDTO();
        invalidUser.setUsername(""); // Invalid username
        invalidUser.setEmail("invalid-email"); // Invalid email
        invalidUser.setPassword("short"); // Invalid password

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }
}