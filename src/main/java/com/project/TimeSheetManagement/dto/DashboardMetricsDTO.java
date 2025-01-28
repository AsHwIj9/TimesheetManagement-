package com.project.TimeSheetManagement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDTO {
    @NotNull
    private Integer activeProjects;

    @NotNull
    private Integer totalResources;

    @NotNull
    private Integer totalBilledHours;

    @NotNull
    private Double averageUtilization;

    @Size(max = 10, message = "Top projects list cannot exceed 10 items.")
    private List<ProjectStatsDTO> topProjects;

    @Size(max = 10, message = "Top resources list cannot exceed 10 items.")
    private List<UserWeeklyStatsDTO> topResources;
}
