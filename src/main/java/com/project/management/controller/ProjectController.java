package com.project.management.controller;

import com.project.management.Models.Project;
import com.project.management.Models.ProjectStatus;
import com.project.management.Models.User;
import com.project.management.dto.*;
import com.project.management.exception.ResourceNotFoundException;
import com.project.management.repository.ProjectRepository;
import com.project.management.repository.UserRepository;
import com.project.management.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects")
@Slf4j
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;


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
    public ResponseEntity<List<ProjectDTO>> getAllProjects() throws Exception {
        log.info("Fetching all projects...");
        List<ProjectDTO> projects = projectService.getAllProjects();
        if (projects.isEmpty()) {
            log.info("No projects found");
            return ResponseEntity.ok(Collections.emptyList());
        }
        log.info("Fetched {} projects", projects.size());
        return ResponseEntity.ok(projects);
    }



    @PostMapping("/{projectName}/users")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<ApiResponse<Void>> assignUsersToProject(
            @PathVariable String projectName,
            @Valid @RequestBody ProjectAssignmentDTO assignmentDTO) {

        log.info("Fetching project ID for project name: {}", assignmentDTO.getProjectName());
        Project project = projectRepository.findByName(assignmentDTO.getProjectName())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + assignmentDTO.getProjectName()));

        log.info("Fetching user IDs for usernames: {}", assignmentDTO.getUserNames());
        List<User> users = userRepository.findAllUserIdByUsername(assignmentDTO.getUserNames());
        if (users.size() != assignmentDTO.getUserNames().size()) {
            throw new ResourceNotFoundException("One or more users not found");
        }

        List<String> userIds = users.stream().map(User::getId).toList();
        log.info("Assigning users to project with ID: {}. User IDs: {}", project.getId(), userIds);

        projectService.assignUsersToProject(project.getId(), userIds);
        log.info("Users assigned successfully to project with ID: {}", project.getId());

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

    @PatchMapping("/{projectId}/status")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<ApiResponse<ProjectDTO>> updateProjectStatus(
            @PathVariable String projectId,
            @RequestParam ProjectStatus status) {
        log.info("Updating project {} to status {}", projectId, status);
        ProjectDTO updatedProject = projectService.updateProjectStatus(projectId, status);
        log.info("Project {} updated successfully to status {}", projectId, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Project status updated successfully", updatedProject));
    }
}
