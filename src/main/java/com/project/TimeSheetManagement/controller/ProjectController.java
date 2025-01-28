package com.project.TimeSheetManagement.controller;

import com.project.TimeSheetManagement.dto.*;
import com.project.TimeSheetManagement.service.ProjectService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Slf4j
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<ApiResponse<ProjectDTO>> createProject(@Valid @RequestBody ProjectDTO projectDTO) {
        log.info("Creating a new project with details: {}", projectDTO);
        ProjectDTO createdProject = projectService.createProject(projectDTO);
        log.info("Project created successfully: {}", createdProject);
        return ResponseEntity.ok(new ApiResponse<>(true, "Project created successfully", createdProject));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority(@roleProperties.adminRole, @roleProperties.userRole)")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        log.info("Fetching all projects...");
        List<ProjectDTO> projects = projectService.getAllProjects();
        log.info("Fetched {} projects", projects.size());
        return ResponseEntity.ok(projects);
    }

    @PostMapping("/{projectId}/users")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<ApiResponse<Void>> assignUsersToProject(
            @PathVariable String projectId,
            @Valid @RequestBody ProjectAssignmentDTO assignmentDTO) {
        log.info("Assigning users to project with ID: {}. User IDs: {}", projectId, assignmentDTO.getUserIds());
        projectService.assignUsersToProject(projectId, assignmentDTO.getUserIds());
        log.info("Users assigned successfully to project with ID: {}", projectId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Users assigned successfully"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority(@roleProperties.adminRole, @roleProperties.userRole)")
    public ResponseEntity<List<ProjectStatsDTO>> getProjectStats() {
        log.info("Fetching project stats...");
        List<ProjectStatsDTO> stats = projectService.getProjectStats();
        log.info("Fetched stats for {} projects", stats.size());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyAuthority(@roleProperties.adminRole, @roleProperties.userRole)")
    public ResponseEntity<ProjectDetailDTO> getProjectDetails(@PathVariable String projectId) {
        log.info("Fetching details for project with ID: {}", projectId);
        ProjectDetailDTO details = projectService.getProjectDetails(projectId);
        log.info("Fetched details for project with ID: {}", projectId);
        return ResponseEntity.ok(details);
    }
}