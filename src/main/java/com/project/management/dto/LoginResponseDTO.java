package com.project.management.dto;

import com.project.management.Models.UserRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    @NotEmpty(message = "Token cannot be empty.")
    private String token;

    @NotEmpty(message = "Username cannot be empty.")
    private String username;

    @NotNull(message = "Role cannot be null.")
    private UserRole role;

    private List<String> assignedProjects;
}
