package com.project.management.controller;

import com.project.management.Models.ProjectStatus;
import com.project.management.Models.User;
import com.project.management.dto.*;
import com.project.management.exception.ResourceNotFoundException;
import com.project.management.repository.ProjectRepository;
import com.project.management.repository.UserRepository;
import com.project.management.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProjectControllerTest {

    @Autowired
    private ProjectController projectController;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private UserRepository userRepository;

    private ProjectDTO projectDTO;
    private final String testProjectId = "proj-123";
    private final String testProjectName = "Test Project";

    @BeforeEach
    void setUp() {
        // Create project DTO for testing
        projectDTO = new ProjectDTO();
        projectDTO.setId(testProjectId);
        projectDTO.setName(testProjectName);
        projectDTO.setStatus(ProjectStatus.ACTIVE);
        projectDTO.setDescription("Test Description");
    }





    private void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createProject_Success() {
        // Arrange
        when(projectService.createProject(any(ProjectDTO.class))).thenReturn(projectDTO);

        // Act
        ResponseEntity<ApiResponse<ProjectDTO>> response = projectController.createProject(projectDTO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Project created successfully");
        assertThat(response.getBody().getData().getId()).isEqualTo(testProjectId);
        assertThat(response.getBody().getData().getName()).isEqualTo(testProjectName);

        verify(projectService).createProject(any(ProjectDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllProjects_Success() throws Exception {
        // Arrange
        List<ProjectDTO> projectList = Collections.singletonList(projectDTO);
        when(projectService.getAllProjects()).thenReturn(projectList);

        // Act
        ResponseEntity<List<ProjectDTO>> response = projectController.getAllProjects();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getId()).isEqualTo(testProjectId);
        assertThat(response.getBody().get(0).getName()).isEqualTo(testProjectName);

        verify(projectService).getAllProjects();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllProjects_EmptyList() throws Exception {
        // Arrange
        when(projectService.getAllProjects()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<ProjectDTO>> response = projectController.getAllProjects();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();

        verify(projectService).getAllProjects();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void assignUsersToProject_Success() {
        // Arrange
        ProjectAssignmentDTO assignmentDTO = new ProjectAssignmentDTO();
        assignmentDTO.setProjectName(testProjectName);
        List<String> usernames = List.of("user1", "user2");
        assignmentDTO.setUserNames(usernames);

        com.project.management.Models.Project project = new com.project.management.Models.Project();
        project.setId(testProjectId);
        project.setName(testProjectName);

        User user1 = new User();
        user1.setId("user1-id");
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId("user2-id");
        user2.setUsername("user2");

        List<User> users = List.of(user1, user2);

        when(projectRepository.findByName(testProjectName)).thenReturn(Optional.of(project));
        when(userRepository.findAllUserIdByUsername(anyList())).thenReturn(users);
        doNothing().when(projectService).assignUsersToProject(anyString(), anyList());

        // Act
        ResponseEntity<ApiResponse<Void>> response = projectController.assignUsersToProject(
                testProjectName, assignmentDTO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Users assigned successfully");

        verify(projectRepository).findByName(testProjectName);
        verify(userRepository).findAllUserIdByUsername(usernames);
        verify(projectService).assignUsersToProject(eq(testProjectId), anyList());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void assignUsersToProject_ProjectNotFound() {
        // Arrange
        ProjectAssignmentDTO assignmentDTO = new ProjectAssignmentDTO();
        assignmentDTO.setProjectName("NonExistentProject");
        assignmentDTO.setUserNames(List.of("user1", "user2"));

        when(projectRepository.findByName("NonExistentProject")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                projectController.assignUsersToProject("NonExistentProject", assignmentDTO)
        );

        verify(projectRepository).findByName("NonExistentProject");
        verify(userRepository, never()).findAllUserIdByUsername(anyList());
        verify(projectService, never()).assignUsersToProject(anyString(), anyList());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void assignUsersToProject_UsersNotFound() {
        // Arrange
        ProjectAssignmentDTO assignmentDTO = new ProjectAssignmentDTO();
        assignmentDTO.setProjectName(testProjectName);
        List<String> usernames = List.of("user1", "user2");
        assignmentDTO.setUserNames(usernames);

        com.project.management.Models.Project project = new com.project.management.Models.Project();
        project.setId(testProjectId);
        project.setName(testProjectName);

        // Return only one user when two are requested - indicating one is not found
        User user1 = new User();
        user1.setId("user1-id");
        user1.setUsername("user1");
        List<User> users = List.of(user1);

        when(projectRepository.findByName(testProjectName)).thenReturn(Optional.of(project));
        when(userRepository.findAllUserIdByUsername(usernames)).thenReturn(users);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                projectController.assignUsersToProject(testProjectName, assignmentDTO)
        );

        verify(projectRepository).findByName(testProjectName);
        verify(userRepository).findAllUserIdByUsername(usernames);
        verify(projectService, never()).assignUsersToProject(anyString(), anyList());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getProjectDetails_Success() {
        // Arrange
        ProjectDetailDTO detailDTO = new ProjectDetailDTO();
        detailDTO.setProjectId(testProjectId);
        detailDTO.setProjectName(testProjectName);
        detailDTO.setDescription("Test Description");
        detailDTO.setStatus(ProjectStatus.ACTIVE);

        when(projectService.getProjectDetails(testProjectId)).thenReturn(detailDTO);

        // Act
        ResponseEntity<ProjectDetailDTO> response = projectController.getProjectDetails(testProjectId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getProjectId()).isEqualTo(testProjectId);
        assertThat(response.getBody().getProjectName()).isEqualTo(testProjectName);
        assertThat(response.getBody().getDescription()).isEqualTo("Test Description");
        assertThat(response.getBody().getStatus()).isEqualTo(ProjectStatus.ACTIVE);

        verify(projectService).getProjectDetails(testProjectId);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getProjectDetails_NotFound() {
        // Arrange
        when(projectService.getProjectDetails("nonexistent"))
                .thenThrow(new ResourceNotFoundException("Project not found"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                projectController.getProjectDetails("nonexistent")
        );

        verify(projectService).getProjectDetails("nonexistent");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateProjectStatus_Success() {
        // Arrange
        projectDTO.setStatus(ProjectStatus.COMPLETED);
        when(projectService.updateProjectStatus(testProjectId, ProjectStatus.COMPLETED))
                .thenReturn(projectDTO);

        // Act
        ResponseEntity<ApiResponse<ProjectDTO>> response =
                projectController.updateProjectStatus(testProjectId, ProjectStatus.COMPLETED);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Project status updated successfully");
        assertThat(response.getBody().getData().getStatus()).isEqualTo(ProjectStatus.COMPLETED);

        verify(projectService).updateProjectStatus(testProjectId, ProjectStatus.COMPLETED);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getProjectStats_Success() {
        // Arrange
        ProjectStatsDTO statsDTO = new ProjectStatsDTO();
        statsDTO.setProjectId(testProjectId);
        statsDTO.setProjectName(testProjectName);
        statsDTO.setActiveResourceCount(5);
        statsDTO.setTotalBilledHours(120);
        statsDTO.setProjectProgress(75.0);
        statsDTO.setActiveResources(List.of("user1", "user2", "user3"));

        List<ProjectStatsDTO> statsList = List.of(statsDTO);
        when(projectService.getProjectStats()).thenReturn(statsList);

        // Act
        ResponseEntity<List<ProjectStatsDTO>> response = projectController.getProjectStats();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getProjectId()).isEqualTo(testProjectId);
        assertThat(response.getBody().get(0).getProjectName()).isEqualTo(testProjectName);
        assertThat(response.getBody().get(0).getActiveResourceCount()).isEqualTo(5);
        assertThat(response.getBody().get(0).getTotalBilledHours()).isEqualTo(120);
        assertThat(response.getBody().get(0).getProjectProgress()).isEqualTo(75.0);
        assertThat(response.getBody().get(0).getActiveResources()).containsExactly("user1", "user2", "user3");

        verify(projectService).getProjectStats();
    }

    @Test
    void accessWithoutAuthentication_Failure() {
        // Arrange
        clearAuthentication();

        // Act & Assert
        assertThrows(Exception.class, () ->
                projectController.getAllProjects()
        );

        verify(projectService, never()).getAllProjects();
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void accessWithInsufficientRole_Failure() {
        // Act & Assert
        assertThrows(Exception.class, () ->
                projectController.createProject(projectDTO)
        );

        verify(projectService, never()).createProject(any(ProjectDTO.class));
    }



    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void assignUsersToProject_ValidationFailure() {
        // Arrange
        ProjectAssignmentDTO invalidAssignment = new ProjectAssignmentDTO();
        assertThrows(Exception.class, () ->
                projectController.assignUsersToProject(testProjectName, invalidAssignment)
        );
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateProjectStatus_InvalidStatus() {
        // Arrange
        when(projectService.updateProjectStatus(anyString(), any(ProjectStatus.class)))
                .thenThrow(new IllegalArgumentException("Invalid status"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                projectController.updateProjectStatus(testProjectId, ProjectStatus.ACTIVE)
        );

        verify(projectService).updateProjectStatus(testProjectId, ProjectStatus.ACTIVE);
    }
}