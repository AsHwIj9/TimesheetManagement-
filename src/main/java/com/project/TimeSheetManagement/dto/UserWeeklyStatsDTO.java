package com.project.TimeSheetManagement.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWeeklyStatsDTO {
    @NotEmpty(message = "User ID cannot be empty.")
    private String userId;

    @NotEmpty(message = "Username cannot be empty.")
    private String username;

    @NotNull(message = "Week start date cannot be null.")
    private LocalDate weekStartDate;

    private Map<String, Integer> projectHours;

    @NotNull(message = "Total hours cannot be null.")
    private Integer totalHours;

    private Double utilizationPercentage;

}
