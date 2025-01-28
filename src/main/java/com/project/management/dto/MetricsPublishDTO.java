package com.project.management.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricsPublishDTO {
    @NotNull(message = "Report date cannot be null.")
    private LocalDate reportDate;

    @NotEmpty(message = "Project metrics cannot be empty.")
    private List<ProjectStatsDTO> projectMetrics;

    private List<UserWeeklyStatsDTO> resourceMetrics;

    private String comments;
}
