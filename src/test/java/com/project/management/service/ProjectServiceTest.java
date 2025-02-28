package com.project.management.service;

import com.project.management.Models.Project;
import com.project.management.Models.ProjectStatus;
import com.project.management.Models.Timesheet;
import com.project.management.Models.User;
import com.project.management.Models.TimeSheetStatus;
import com.project.management.dto.ProjectDTO;
import com.project.management.dto.ProjectDetailDTO;
import com.project.management.dto.ProjectStatsDTO;
import com.project.management.dto.UserDTO;
import com.project.management.dto.TimesheetSummaryDTO;
import com.project.management.exception.ResourceNotFoundException;
import com.project.management.exception.UserAlreadyExistsException;
import com.project.management.mapper.ProjectMapper;
import com.project.management.repository.ProjectRepository;
import com.project.management.repository.TimesheetRepository;
import com.project.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.EnumMap;

import static com.project.management.Models.UserRole.USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;
    private ProjectDTO testProjectDTO;
    private User testUser;
    private Timesheet testTimesheet;

    @BeforeEach
    void setup() {
        // Setup test project
        testProject = new Project();
        testProject.setId("project123");
        testProject.setName("Test Project");
        testProject.setDescription("This is a test project");
        testProject.setStartDate(LocalDateTime.now());
        testProject.setEndDate(LocalDateTime.now().plusMonths(3));
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setAssignedUsers(new ArrayList<>(Arrays.asList("user1", "user2")));
        testProject.setTotalBudgetHours(100);
        testProject.setTotalBilledHours(20);

        // Setup test project DTO
        testProjectDTO = new ProjectDTO();
        testProjectDTO.setId("project123");
        testProjectDTO.setName("Test Project");
        testProjectDTO.setDescription("This is a test project");
        testProjectDTO.setStartDate(LocalDateTime.now());
        testProjectDTO.setEndDate(LocalDateTime.now().plusMonths(3));
        testProjectDTO.setStatus(ProjectStatus.ACTIVE);
        testProjectDTO.setAssignedUsers(new ArrayList<>(Arrays.asList("user1", "user2")));
        testProjectDTO.setTotalBudgetHours(100);
        testProjectDTO.setTotalBilledHours(20);

        // Setup test user
        testUser = new User();
        testUser.setId("user3");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(USER); // Fixed: Using String instead of DEVELOPER enum
        testUser.setAssignedProjects(new ArrayList<>());
        testUser.setCreatedAt(LocalDateTime.now());

        // Setup test timesheet
        testTimesheet = new Timesheet();
        testTimesheet.setId("timesheet123");
        testTimesheet.setUserId("user1");
        testTimesheet.setProjectId("project123");
        testTimesheet.setWeekStartDate(LocalDate.now().minusDays(7));

        // Using EnumMap instead of HashMap
        Map<DayOfWeek, Integer> dailyHours = new EnumMap<>(DayOfWeek.class);
        dailyHours.put(DayOfWeek.MONDAY, 8);
        dailyHours.put(DayOfWeek.TUESDAY, 7);
        dailyHours.put(DayOfWeek.WEDNESDAY, 8);
        dailyHours.put(DayOfWeek.THURSDAY, 6);
        dailyHours.put(DayOfWeek.FRIDAY, 6);
        testTimesheet.setDailyHours(dailyHours);
        testTimesheet.setDescription("Weekly work");
        testTimesheet.setStatus(TimeSheetStatus.APPROVED);
        testTimesheet.setSubmittedAt(LocalDateTime.now().minusDays(2));

        // Set up common mocks
        when(projectMapper.toProjectDTO(any(Project.class))).thenReturn(testProjectDTO);
    }

    @Test
    void testCreateProject() {
        // Arrange
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // Act
        ProjectDTO result = projectService.createProject(testProjectDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testProjectDTO.getName(), result.getName());
        assertEquals(testProjectDTO.getDescription(), result.getDescription());
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(projectMapper, times(1)).toProjectDTO(any(Project.class));
    }

    @Test
    void testAssignUsersToProject() {
        // Arrange
        List<String> newUserIds = Collections.singletonList("user3");
        List<User> users = Collections.singletonList(testUser);

        when(projectRepository.findById(anyString())).thenReturn(Optional.of(testProject));
        when(userRepository.findAllById(anyList())).thenReturn(users);

        // Act
        projectService.assignUsersToProject("project123", newUserIds);

        // Assert
        assertTrue(testProject.getAssignedUsers().contains("user3"));
        assertTrue(testUser.getAssignedProjects().contains("project123"));
        verify(projectRepository, times(1)).save(testProject);
        verify(userRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testAssignUsersToProject_ProjectNotFound() {
        // Arrange
        when(projectRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.assignUsersToProject("nonexistent", Collections.singletonList("user3"));
        });
    }

    @Test
    void testAssignUsersToProject_UsersAlreadyAssigned() {
        // Arrange
        List<String> existingUserIds = Arrays.asList("user1", "user2");

        when(projectRepository.findById(anyString())).thenReturn(Optional.of(testProject));
        when(userRepository.findAllById(anyList())).thenReturn(Arrays.asList(
                createTestUser("user1"), createTestUser("user2")
        ));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            projectService.assignUsersToProject("project123", existingUserIds);
        });
    }

    @Test
    void testUpdateProjectStatus() {
        // Arrange
        when(projectRepository.findById(anyString())).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // Act
        ProjectDTO result = projectService.updateProjectStatus("project123", ProjectStatus.COMPLETED);

        // Assert
        assertEquals(ProjectStatus.COMPLETED, testProject.getStatus());
        assertNotNull(result);
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void testUpdateProjectStatus_InvalidTransition() {
        // Arrange
        when(projectRepository.findById(anyString())).thenReturn(Optional.of(testProject));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.updateProjectStatus("project123", ProjectStatus.ACTIVE);
        });
    }

    @Test
    void testGetAllProjects() {
        // Arrange
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findAll()).thenReturn(projects);

        // Act
        List<ProjectDTO> result = projectService.getAllProjects();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectRepository, times(1)).findAll();
        verify(projectMapper, times(1)).toProjectDTO(any(Project.class));
    }

    @Test
    void testGetProjectStats() {
        // Arrange
        List<Project> projects = Collections.singletonList(testProject);
        List<Timesheet> timesheets = Collections.singletonList(testTimesheet);

        when(projectRepository.findAll()).thenReturn(projects);
        when(timesheetRepository.findByProjectId(anyString())).thenReturn(timesheets);

        // Act
        List<ProjectStatsDTO> result = projectService.getProjectStats();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("project123", result.get(0).getProjectId());
        assertEquals("Test Project", result.get(0).getProjectName());
        assertEquals(1, result.get(0).getActiveResourceCount());
        assertEquals(20, result.get(0).getTotalBilledHours());
        verify(projectRepository, times(1)).findAll();
        verify(timesheetRepository, times(1)).findByProjectId(anyString());
    }

    @Test
    void testGetProjectDetails() {
        // Arrange
        List<User> assignedUsers = Arrays.asList(
                createTestUser("user1"),
                createTestUser("user2")
        );
        List<Timesheet> timesheets = Collections.singletonList(testTimesheet);

        when(projectRepository.findById(anyString())).thenReturn(Optional.of(testProject));
        when(userRepository.findAllById(anyList())).thenReturn(assignedUsers);
        when(timesheetRepository.findByProjectId(anyString())).thenReturn(timesheets);

        // Act
        ProjectDetailDTO result = projectService.getProjectDetails("project123");

        // Assert
        assertNotNull(result);
        assertEquals("project123", result.getProjectId());
        assertEquals("Test Project", result.getProjectName());
        assertEquals(ProjectStatus.ACTIVE, result.getStatus());
        assertEquals(2, result.getAssignedUsers().size());
        assertEquals(1, result.getRecentTimesheets().size());
        verify(projectRepository, times(1)).findById(anyString());
        verify(userRepository, times(1)).findAllById(anyList());
        verify(timesheetRepository, times(1)).findByProjectId(anyString());
    }

    @Test
    void testGetProjectDetails_ProjectNotFound() {
        // Arrange
        when(projectRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectDetails("nonexistent");
        });
    }

    private User createTestUser(String userId) {
        User user = new User();
        user.setId(userId);
        user.setUsername("user" + userId);
        user.setEmail("user" + userId + "@example.com");
        user.setPassword("password");
        user.setRole(USER);
        user.setAssignedProjects(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}