package com.project.management.service;

import com.project.management.dto.DashboardMetricsDTO;
import com.project.management.dto.ProjectStatsDTO;
import com.project.management.dto.UserWeeklyStatsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class MetricsServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @InjectMocks
    private MetricsService metricsService;

    private List<ProjectStatsDTO> mockProjects;
    private List<UserWeeklyStatsDTO> mockUsers;

    @BeforeEach
    void setUp() {
        // Setup mock project data
        mockProjects = Arrays.asList(
                new ProjectStatsDTO("P1", "Project 1", 5, 100, 75.0, Arrays.asList("User1", "User2")),
                new ProjectStatsDTO("P2", "Project 2", 3, 80, 60.0, Arrays.asList("User3")),
                new ProjectStatsDTO("P3", "Project 3", 0, 0, 0.0, Arrays.asList()),
                new ProjectStatsDTO("P4", "Project 4", 4, 120, 90.0, Arrays.asList("User4", "User5")),
                new ProjectStatsDTO("P5", "Project 5", 2, 40, 30.0, Arrays.asList("User6"))
        );

        // Setup mock user data
        Map<String, Integer> projectHours1 = new HashMap<>();
        projectHours1.put("P1", 40);
        Map<String, Integer> projectHours2 = new HashMap<>();
        projectHours2.put("P2", 35);
        Map<String, Integer> projectHours3 = new HashMap<>();
        projectHours3.put("P4", 30);

        mockUsers = Arrays.asList(
                new UserWeeklyStatsDTO("U1", "User 1", LocalDate.now(), projectHours1, 40, 100.0),
                new UserWeeklyStatsDTO("U2", "User 2", LocalDate.now(), projectHours2, 35, 87.5),
                new UserWeeklyStatsDTO("U3", "User 3", LocalDate.now(), projectHours3, 30, 75.0)
        );
    }

    @Test
    void getDashboardMetrics_ShouldReturnCorrectMetrics() {
        // Arrange
        when(projectService.getProjectStats()).thenReturn(mockProjects);
        when(userService.getUsersWeeklyStats(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockUsers);

        // Act
        DashboardMetricsDTO result = metricsService.getDashboardMetrics();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getActiveProjects()); // Projects with activeResourceCount > 0
        assertEquals(3, result.getTotalResources()); // Size of mockUsers
        assertEquals(340, result.getTotalBilledHours()); // Sum of all project billed hours
        assertEquals(87.5, result.getAverageUtilization()); // Average of user utilization

        // Verify top projects
        assertEquals(5, result.getTopProjects().size());
        assertEquals("P4", result.getTopProjects().get(0).getProjectId()); // Highest billed hours
        assertEquals("P1", result.getTopProjects().get(1).getProjectId()); // Second highest

        // Verify top resources
        assertEquals(3, result.getTopResources().size());
        assertEquals("U1", result.getTopResources().get(0).getUserId()); // Highest utilization
        assertEquals("U2", result.getTopResources().get(1).getUserId()); // Second highest
    }

    @Test
    void getDashboardMetrics_WithEmptyData_ShouldHandleGracefully() {
        // Arrange
        when(projectService.getProjectStats()).thenReturn(Arrays.asList());
        when(userService.getUsersWeeklyStats(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act
        DashboardMetricsDTO result = metricsService.getDashboardMetrics();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getActiveProjects());
        assertEquals(0, result.getTotalResources());
        assertEquals(0, result.getTotalBilledHours());
        assertEquals(0.0, result.getAverageUtilization());
        assertTrue(result.getTopProjects().isEmpty());
        assertTrue(result.getTopResources().isEmpty());
    }

    @Test
    void getDashboardMetrics_WithNullValues_ShouldHandleGracefully() {
        // Arrange
        List<ProjectStatsDTO> projectsWithNull = Arrays.asList(
                new ProjectStatsDTO("P1", "Project 1", null, null, null, null),
                new ProjectStatsDTO("P2", "Project 2", 0, null, null, null)
        );

        List<UserWeeklyStatsDTO> usersWithNull = Arrays.asList(
                new UserWeeklyStatsDTO("U1", "User 1", LocalDate.now(), null, null, null)
        );

        when(projectService.getProjectStats()).thenReturn(projectsWithNull);
        when(userService.getUsersWeeklyStats(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(usersWithNull);

        // Act & Assert
        assertDoesNotThrow(() -> {
            DashboardMetricsDTO result = metricsService.getDashboardMetrics();
            assertNotNull(result);
        });
    }
}