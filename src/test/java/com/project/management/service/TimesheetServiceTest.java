package com.project.management.service;

import com.project.management.dto.*;
import com.project.management.Models.*;
import com.project.management.exception.ResourceNotFoundException;
import com.project.management.repository.ProjectRepository;
import com.project.management.repository.TimesheetRepository;
import com.project.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimesheetServiceTest {

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TimesheetService timesheetService;

    private TimesheetDTO timesheetDTO;
    private Timesheet timesheet;
    private Project project;
    private User user;
    private EnumMap<DayOfWeek, Integer> dailyHours;
    private static final String USER_ID = "user123";
    private static final String PROJECT_ID = "project123";

    @BeforeEach
    void setUp() {
        // Setup daily hours using EnumMap
        dailyHours = new EnumMap<>(DayOfWeek.class);
        dailyHours.put(DayOfWeek.MONDAY, 8);
        dailyHours.put(DayOfWeek.TUESDAY, 8);
        dailyHours.put(DayOfWeek.WEDNESDAY, 8);
        dailyHours.put(DayOfWeek.THURSDAY, 8);
        dailyHours.put(DayOfWeek.FRIDAY, 8);

        // Setup Project
        project = new Project();
        project.setId(PROJECT_ID);
        project.setName("Test Project");
        project.setAssignedUsers(Collections.singletonList(USER_ID));
        project.setTotalBilledHours(0);

        // Setup User
        user = new User(
                USER_ID,
                "testuser",
                "test@example.com",
                "password",
                UserRole.USER, // Assuming UserRole.USER is the correct enum value
                Collections.singletonList(PROJECT_ID),
                LocalDateTime.now()
        );

        // Setup TimesheetDTO
        timesheetDTO = new TimesheetDTO(
                null,
                USER_ID,
                PROJECT_ID,
                LocalDate.now(),
                dailyHours,
                "Test timesheet",
                TimeSheetStatus.SUBMITTED,
                LocalDateTime.now()
        );

        // Setup Timesheet
        timesheet = new Timesheet();
        timesheet.setId("timesheet123");
        timesheet.setUserId(USER_ID);
        timesheet.setProjectId(PROJECT_ID);
        timesheet.setWeekStartDate(LocalDate.now());
        timesheet.setDailyHours(dailyHours);
        timesheet.setDescription("Test timesheet");
        timesheet.setStatus(TimeSheetStatus.SUBMITTED);
        timesheet.setSubmittedAt(LocalDateTime.now());
    }

    @Test
    void submitTimesheet_Success() {
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(timesheetRepository.existsByUserIdAndProjectIdAndWeekStartDate(
                any(), any(), any())).thenReturn(false);
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(timesheet);

        TimesheetDTO result = timesheetService.submitTimesheet(timesheetDTO);

        assertNotNull(result);
        assertEquals(TimeSheetStatus.SUBMITTED, result.getStatus());
        assertEquals(USER_ID, result.getUserId());
        assertEquals(PROJECT_ID, result.getProjectId());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void submitTimesheet_UserNotAssignedToProject() {
        Project unassignedProject = new Project();
        unassignedProject.setId(PROJECT_ID);
        unassignedProject.setAssignedUsers(Collections.emptyList());

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(unassignedProject));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> timesheetService.submitTimesheet(timesheetDTO));
    }

    @Test
    void approveTimesheet_Success() {
        when(timesheetRepository.findById("timesheet123")).thenReturn(Optional.of(timesheet));
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(timesheet);

        TimesheetDTO result = timesheetService.approveTimesheet("timesheet123");

        assertNotNull(result);
        assertEquals(TimeSheetStatus.APPROVED, result.getStatus());
    }

    @Test
    void rejectTimesheet_Success() {
        when(timesheetRepository.findById("timesheet123")).thenReturn(Optional.of(timesheet));
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(timesheet);

        TimesheetDTO result = timesheetService.rejectTimesheet("timesheet123", "Incorrect hours");

        assertNotNull(result);
        assertEquals(TimeSheetStatus.REJECTED, result.getStatus());
        assertTrue(result.getDescription().contains("Incorrect hours"));
    }

    @Test
    void getUserTimesheets_Success() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<Timesheet> timesheets = Collections.singletonList(timesheet);

        when(timesheetRepository.findByUserIdAndWeekStartDateBetween(
                USER_ID, startDate, endDate)).thenReturn(timesheets);

        List<TimesheetSummaryDTO> result = timesheetService.getUserTimesheets(
                USER_ID, startDate, endDate);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(USER_ID, timesheet.getUserId());
    }

    @Test
    void getTimesheetStats_Success() {
        List<Timesheet> timesheets = Collections.singletonList(timesheet);
        when(timesheetRepository.findAll()).thenReturn(timesheets);
        when(timesheetRepository.findByWeekStartDateAfter(any())).thenReturn(timesheets);

        TimesheetStatsDTO result = timesheetService.getTimesheetStats();

        assertNotNull(result);
        assertEquals(1, result.getTotalSubmittedTimesheets());
        assertEquals(40, result.getTotalBilledHours()); // 5 days * 8 hours
        assertTrue(result.getHoursPerProject().containsKey(PROJECT_ID));
        assertTrue(result.getHoursPerUser().containsKey(USER_ID));
    }

    @Test
    void getTimesheetById_NotFound() {
        String nonExistentId = "nonexistent123";
        when(timesheetRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> timesheetService.getTimesheetById(nonExistentId));
    }

    @Test
    void approveTimesheet_InvalidStatus() {
        Timesheet approvedTimesheet = new Timesheet();
        approvedTimesheet.setId("timesheet123");
        approvedTimesheet.setStatus(TimeSheetStatus.APPROVED);

        when(timesheetRepository.findById("timesheet123")).thenReturn(Optional.of(approvedTimesheet));

        assertThrows(IllegalStateException.class,
                () -> timesheetService.approveTimesheet("timesheet123"));
    }
}