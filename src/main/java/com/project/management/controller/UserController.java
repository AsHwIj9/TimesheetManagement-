package com.project.management.controller;

import com.project.management.dto.*;
import com.project.management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;


    @PostMapping
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserRegistrationDTO userDTO) {
        log.info("Creating a new user with details: {}", userDTO);
        UserDTO createdUser = userService.createUser(userDTO);
        log.info("User created successfully: {}", createdUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "User created successfully", createdUser));
    }

    @GetMapping
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Fetching all users...");
        List<UserDTO> users = userService.getAllUsers();
        log.info("Fetched {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        log.info("Fetching details for user with ID: {}", userId);
        UserDTO user = userService.getUserById(userId);
        log.info("Fetched user details: {}", user);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/stats/weekly")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<List<UserWeeklyStatsDTO>> getUsersWeeklyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching weekly stats for users from {} to {}", startDate, endDate);
        List<UserWeeklyStatsDTO> stats = userService.getUsersWeeklyStats(startDate, endDate);
        log.info("Fetched weekly stats for {} users", stats.size());
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String userId) {
        log.info("Deleting user with ID: {}", userId);
        userService.deleteUserById(userId);
        log.info("User with ID {} deleted successfully", userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", userId));
    }
}
