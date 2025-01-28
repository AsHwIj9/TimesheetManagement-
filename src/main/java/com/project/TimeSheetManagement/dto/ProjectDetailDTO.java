package com.project.TimeSheetManagement.dto;

import com.project.TimeSheetManagement.Models.ProjectStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailDTO {
    @NotEmpty(message = "Project ID cannot be empty.")
    private String projectId;

    @NotEmpty(message = "Project name cannot be empty.")
    private String projectName;

    private String description;

    @NotNull(message = "Project status cannot be null.")
    private ProjectStatus status;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private List<UserDTO> assignedUsers;

    private Integer totalBudgetHours;
    private Integer totalBilledHours;

    private List<TimesheetSummaryDTO> recentTimesheets;
}

