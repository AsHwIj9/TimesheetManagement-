package com.project.management.service;

import com.project.management.Models.Project;
import com.project.management.Models.ProjectStatus;
import com.project.management.Models.Timesheet;
import com.project.management.Models.User;
import com.project.management.dto.ProjectDTO;
import com.project.management.dto.ProjectDetailDTO;
import com.project.management.dto.ProjectStatsDTO;
import com.project.management.exception.ResourceNotFoundException;
import com.project.management.mapper.ProjectMapper;
import com.project.management.repository.ProjectRepository;
import com.project.management.repository.TimesheetRepository;
import com.project.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TimesheetRepository timesheetRepository;

    @MockBean
    private ProjectMapper projectMapper;

    private Project testProject;
    private ProjectDTO testProjectDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId("1");
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setStartDate(LocalDateTime.now());
        testProject.setEndDate(LocalDateTime.now().plusMonths(1));
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setAssignedUsers(new ArrayList<>());
        testProject.setTotalBudgetHours(100);
        testProject.setTotalBilledHours(0);

        testProjectDTO = new ProjectDTO();
        testProjectDTO.setName("Test Project");
        testProjectDTO.setDescription("Test Description");
        testProjectDTO.setStartDate(LocalDateTime.now());
        testProjectDTO.setEndDate(LocalDateTime.now().plusMonths(1));
        testProjectDTO.setTotalBudgetHours(100);

        testUser = new User();
        testUser.setId("user1");
        testUser.setUsername("testUser");
        testUser.setAssignedProjects(new ArrayList<>());
    }

    @Test
    void createProject_ShouldCreateAndReturnProject() {
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(projectMapper.toProjectDTO(any(Project.class))).thenReturn(testProjectDTO);

        ProjectDTO result = projectService.createProject(testProjectDTO);

        assertNotNull(result);
        assertEquals(testProjectDTO.getName(), result.getName());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void assignUsersToProject_ShouldAssignUsersSuccessfully() {
        List<String> userIds = List.of("user1");  // Using List.of instead of Arrays.asList
        when(projectRepository.findById("1")).thenReturn(Optional.of(testProject));
        when(userRepository.findAllById(userIds)).thenReturn(List.of(testUser));

        assertDoesNotThrow(() ->
                projectService.assignUsersToProject("1", userIds)
        );

        verify(projectRepository).save(any(Project.class));
        verify(userRepository).saveAll(any());
    }

    @Test
    void assignUsersToProject_ShouldThrowException_WhenProjectNotFound() {
        List<String> userIds = List.of("user1");
        when(projectRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                projectService.assignUsersToProject("1", userIds)
        );
    }

    @Test
    void updateProjectStatus_ShouldUpdateToCompleted() {
        when(projectRepository.findById("1")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(projectMapper.toProjectDTO(any(Project.class))).thenReturn(testProjectDTO);

        ProjectDTO result = projectService.updateProjectStatus("1", ProjectStatus.COMPLETED);

        assertNotNull(result);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void updateProjectStatus_ShouldThrowException_WhenInvalidTransition() {
        when(projectRepository.findById("1")).thenReturn(Optional.of(testProject));

        assertThrows(IllegalArgumentException.class, () ->
                projectService.updateProjectStatus("1", ProjectStatus.ACTIVE)
        );
    }

    @Test
    void getAllProjects_ShouldReturnAllProjects() {
        List<Project> projects = List.of(testProject);
        when(projectRepository.findAll()).thenReturn(projects);
        when(projectMapper.toProjectDTO(any(Project.class))).thenReturn(testProjectDTO);

        List<ProjectDTO> result = projectService.getAllProjects();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getProjectStats_ShouldReturnProjectStats() {
        List<Project> projects = List.of(testProject);
        List<Timesheet> timesheets = new ArrayList<>();

        when(projectRepository.findAll()).thenReturn(projects);
        when(timesheetRepository.findByProjectId(any())).thenReturn(timesheets);

        List<ProjectStatsDTO> result = projectService.getProjectStats();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testProject.getId(), result.get(0).getProjectId());
    }

    @Test
    void getProjectDetails_ShouldReturnProjectDetails() {
        when(projectRepository.findById("1")).thenReturn(Optional.of(testProject));
        when(userRepository.findAllById(any())).thenReturn(new ArrayList<>());
        when(timesheetRepository.findByProjectId(any())).thenReturn(new ArrayList<>());

        ProjectDetailDTO result = projectService.getProjectDetails("1");

        assertNotNull(result);
        assertEquals(testProject.getId(), result.getProjectId());
        assertEquals(testProject.getName(), result.getProjectName());
    }

    @Test
    void getProjectDetails_ShouldThrowException_WhenProjectNotFound() {
        when(projectRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                projectService.getProjectDetails("1")
        );
    }
}