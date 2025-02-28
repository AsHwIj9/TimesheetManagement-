package com.project.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.management.Models.TimeSheetStatus;
import com.project.management.dto.*;
import com.project.management.service.TimesheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TimesheetControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private TimesheetService timesheetService;

    @Autowired
    private ObjectMapper objectMapper;

    private TimesheetDTO sampleTimesheetDTO;
    private TimesheetResponseDTO sampleTimesheetResponseDTO;
    private final String testUserId = "test-user";
    private final String testProjectId = "test-project";
    private final String testTimesheetId = "test-timesheet-id";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        Map<DayOfWeek, Integer> dailyHours = new HashMap<>();
        dailyHours.put(DayOfWeek.MONDAY, 8);
        dailyHours.put(DayOfWeek.TUESDAY, 8);
        dailyHours.put(DayOfWeek.WEDNESDAY, 8);

        sampleTimesheetDTO = new TimesheetDTO();
        sampleTimesheetDTO.setId(testTimesheetId);
        sampleTimesheetDTO.setUserId(testUserId);
        sampleTimesheetDTO.setProjectId(testProjectId);
        sampleTimesheetDTO.setWeekStartDate(LocalDate.now());
        sampleTimesheetDTO.setDailyHours(dailyHours);
        sampleTimesheetDTO.setDescription("Test timesheet");
        sampleTimesheetDTO.setStatus(TimeSheetStatus.SUBMITTED);
        sampleTimesheetDTO.setSubmittedAt(LocalDateTime.now());

        sampleTimesheetResponseDTO = new TimesheetResponseDTO(
                testTimesheetId,
                testUserId,
                testProjectId,
                LocalDate.now(),
                dailyHours,
                "Test timesheet",
                TimeSheetStatus.SUBMITTED,
                LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void submitTimesheet_Success() throws Exception {
        when(timesheetService.submitTimesheet(any(TimesheetDTO.class))).thenReturn(sampleTimesheetDTO);

        mockMvc.perform(post("/api/timesheets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTimesheetDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Timesheet submitted successfully"))
                .andExpect(jsonPath("$.data.id").value(testTimesheetId));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getTimesheetById_Success() throws Exception {
        when(timesheetService.getTimesheetById(testTimesheetId)).thenReturn(sampleTimesheetResponseDTO);

        mockMvc.perform(get("/api/timesheets/{timesheetId}", testTimesheetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Timesheet retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(testTimesheetId));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void approveTimesheet_Success() throws Exception {
        sampleTimesheetDTO.setStatus(TimeSheetStatus.APPROVED);
        when(timesheetService.approveTimesheet(testTimesheetId)).thenReturn(sampleTimesheetDTO);

        mockMvc.perform(patch("/api/timesheets/{timesheetId}/approve", testTimesheetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Timesheet approved successfully"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void rejectTimesheet_Success() throws Exception {
        sampleTimesheetDTO.setStatus(TimeSheetStatus.REJECTED);
        Map<String, String> rejectionBody = new HashMap<>();
        rejectionBody.put("rejectionReason", "Invalid hours");

        when(timesheetService.rejectTimesheet(eq(testTimesheetId), any(String.class)))
                .thenReturn(sampleTimesheetDTO);

        mockMvc.perform(patch("/api/timesheets/{timesheetId}/reject", testTimesheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectionBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Timesheet rejected successfully"))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void getUserTimesheets_Success() throws Exception {
        List<TimesheetSummaryDTO> timesheets = Collections.singletonList(new TimesheetSummaryDTO());
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        when(timesheetService.getUserTimesheets(testUserId, startDate, endDate))
                .thenReturn(timesheets);

        mockMvc.perform(get("/api/timesheets/users/{userId}", testUserId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getProjectTimesheets_Success() throws Exception {
        List<TimesheetSummaryDTO> timesheets = Collections.singletonList(new TimesheetSummaryDTO());
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        when(timesheetService.getProjectTimesheets(testProjectId, startDate, endDate))
                .thenReturn(timesheets);

        mockMvc.perform(get("/api/timesheets/projects/{projectId}", testProjectId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getProjectTimesheets_InvalidDateRange() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        mockMvc.perform(get("/api/timesheets/projects/{projectId}", testProjectId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getTimesheetStats_Success() throws Exception {
        TimesheetStatsDTO statsDTO = new TimesheetStatsDTO();
        when(timesheetService.getTimesheetStats()).thenReturn(statsDTO);

        mockMvc.perform(get("/api/timesheets/stats/summary"))
                .andExpect(status().isOk());
    }




}