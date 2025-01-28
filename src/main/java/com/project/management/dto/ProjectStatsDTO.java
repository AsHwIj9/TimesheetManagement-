package com.project.management.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatsDTO {
    @NotEmpty(message = "Project ID cannot be empty.")
    private String projectId;

    @NotEmpty(message = "Project name cannot be empty.")
    private String projectName;

    @NotNull(message = "Active resource count cannot be null.")
    private Integer activeResourceCount;

    private Integer totalBilledHours;

    private Double projectProgress;

    private List<String> activeResources;
}

