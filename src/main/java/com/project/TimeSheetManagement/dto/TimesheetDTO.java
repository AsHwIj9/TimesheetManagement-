package com.project.TimeSheetManagement.dto;

import com.project.TimeSheetManagement.Models.TimeSheetStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDTO {
    private String id;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Project ID is required")
    private String projectId;

    @NotNull(message = "Week start date is required")
    private LocalDate weekStartDate;

    @NotNull(message = "Daily hours are required")
    @Valid
    private Map<DayOfWeek, @Min(0) @Max(24) Integer> dailyHours;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private TimeSheetStatus status;
    private LocalDateTime submittedAt;



    @AssertTrue(message = "Total daily hours cannot exceed 24")
    private boolean isValidDailyHours() {
        if (dailyHours == null) {
            return true;
        }
        return dailyHours.values().stream()
                .allMatch(hours -> hours >= 0 && hours <= 24);
    }
}
