package com.project.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAssignmentDTO {

    @NotBlank(message = "Project ID is required")
    private String projectName;

    @NotEmpty(message = "At least one user must be assigned")
    private List<String> userNames;

}

