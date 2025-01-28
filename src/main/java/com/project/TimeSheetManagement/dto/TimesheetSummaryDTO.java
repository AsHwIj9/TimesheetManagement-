package com.project.TimeSheetManagement.dto;

import com.project.TimeSheetManagement.Models.TimeSheetStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetSummaryDTO {
    @NotEmpty(message = "ID cannot be empty.")
    private String id;

    @NotEmpty(message = "Project name cannot be empty.")
    private String projectName;

    @NotNull(message = "Week start date cannot be null.")
    private LocalDate weekStartDate;

    @NotNull(message = "Total hours cannot be null.")
    private Integer totalHours;

    @NotNull(message = "Timesheet status cannot be null.")
    private TimeSheetStatus status;

    private LocalDateTime submittedAt;
}
