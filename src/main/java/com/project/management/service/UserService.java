package com.project.management.service;

import com.project.management.dto.UserDTO;
import com.project.management.dto.UserRegistrationDTO;
import com.project.management.dto.UserWeeklyStatsDTO;
import com.project.management.Models.Timesheet;
import com.project.management.Models.User;
import com.project.management.Models.UserRole;
import com.project.management.repository.TimesheetRepository;
import com.project.management.repository.UserRepository;
import jakarta.validation.ValidationException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Builder
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TimesheetRepository timesheetRepository;

    public UserDTO createUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.findByUsername(registrationDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(UserRole.USER);
        user.setAssignedProjects(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapToDTO(user);
    }

    public List<UserWeeklyStatsDTO> getUsersWeeklyStats(LocalDate startDate, LocalDate endDate) {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> calculateUserWeeklyStats(user, startDate, endDate))
                .collect(Collectors.toList());
    }

    private UserWeeklyStatsDTO calculateUserWeeklyStats(User user, LocalDate startDate, LocalDate endDate) {
        List<Timesheet> timesheets = timesheetRepository
                .findByUserIdAndWeekStartDateBetween(user.getId(), startDate, endDate);

        Map<String, Integer> projectHours = new HashMap<>();
        Integer totalHours = 0;

        for (Timesheet timesheet : timesheets) {
            Integer weeklyHours = timesheet.getDailyHours().values().stream()
                    .reduce(0, Integer::sum);
            totalHours += weeklyHours;

            projectHours.merge(timesheet.getProjectId(), weeklyHours, Integer::sum);
        }

        Double utilization = calculateUtilization(totalHours, startDate, endDate);

        return new UserWeeklyStatsDTO(
                user.getId(),
                user.getUsername(),
                startDate,
                projectHours,
                totalHours,
                utilization
        );
    }

    private Double calculateUtilization(int totalHours, LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int maxHours = (int) (days * 8); // Assuming an 8-hour workday
        return (double) totalHours / maxHours * 100;
    }

    private UserDTO mapToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getAssignedProjects(),
                user.getCreatedAt()
        );
    }
}
