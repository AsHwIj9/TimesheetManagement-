package com.project.management.controller;

import com.project.management.dto.*;
import com.project.management.service.TimesheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/timesheets")
@Slf4j
public class TimesheetController {
    private final TimesheetService timesheetService;


    @PostMapping
    @PreAuthorize("hasAuthority(@roleProperties.userRole)")
    public ResponseEntity<ApiResponse<TimesheetDTO>> submitTimesheet(@Valid @RequestBody TimesheetDTO timesheetDTO) {
        log.info("Submitting timesheet: {}", timesheetDTO);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        timesheetDTO.setUserId(userId);
        log.info("Submitting timesheet: {}", timesheetDTO);
        TimesheetDTO submitted = timesheetService.submitTimesheet(timesheetDTO);
        log.info("Timesheet submitted successfully: {}", submitted);
        return ResponseEntity.ok(new ApiResponse<>(true, "Timesheet submitted successfully", submitted));
    }

    @GetMapping("/{timesheetId}")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<ApiResponse<TimesheetResponseDTO>> getTimesheetById(@PathVariable String timesheetId) {
        log.info("Fetching timesheet with ID: {}", timesheetId);
        TimesheetResponseDTO timesheet = timesheetService.getTimesheetById(timesheetId);
        log.info("Fetched timesheet: {}", timesheet);
        return ResponseEntity.ok(new ApiResponse<>(true, "Timesheet retrieved successfully", timesheet));
    }

    @PatchMapping("/{timesheetId}/approve")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<ApiResponse<TimesheetDTO>> approveTimesheet(@PathVariable String timesheetId) {
        log.info("Approving timesheet with ID: {}", timesheetId);
        TimesheetDTO approved = timesheetService.approveTimesheet(timesheetId);
        log.info("Timesheet approved successfully: {}", approved);
        return ResponseEntity.ok(new ApiResponse<>(true, "Timesheet approved successfully", approved));
    }

    @PatchMapping("/{timesheetId}/reject")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<ApiResponse<TimesheetDTO>> rejectTimesheet(
            @PathVariable String timesheetId,
            @RequestBody Map<String, String> requestBody) {
        String rejectionReason = requestBody.get("rejectionReason");
        log.info("Rejecting timesheet with ID: {} for reason: {}", timesheetId, rejectionReason);
        TimesheetDTO rejected = timesheetService.rejectTimesheet(timesheetId, rejectionReason);
        log.info("Timesheet rejected successfully: {}", rejected);
        return ResponseEntity.ok(new ApiResponse<>(true, "Timesheet rejected successfully", rejected));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<TimesheetSummaryDTO>> getUserTimesheets(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate defaultStartDate = startDate != null ? startDate : LocalDate.now().minusMonths(1);
        LocalDate defaultEndDate = endDate != null ? endDate : LocalDate.now();
        log.info("Fetching timesheets for userId: {}, StartDate: {}, EndDate: {}", userId, defaultStartDate, defaultEndDate);
        List<TimesheetSummaryDTO> timesheets = timesheetService.getUserTimesheets(userId, defaultStartDate, defaultEndDate);
        log.info("Fetched {} timesheets for userId: {}", timesheets.size(), userId);
        return ResponseEntity.ok(timesheets);
    }

    @GetMapping("/projects/{projectId}")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<List<TimesheetSummaryDTO>> getProjectTimesheets(
            @PathVariable String projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            log.warn("Invalid date range: StartDate {} is after EndDate {}", startDate, endDate);
            return ResponseEntity.badRequest().body(null);
        }

        log.info("Fetching project timesheets for projectId: {}, StartDate: {}, EndDate: {}", projectId, startDate, endDate);
        List<TimesheetSummaryDTO> timesheets = timesheetService.getProjectTimesheets(projectId, startDate, endDate);
        log.info("Fetched {} timesheets for projectId: {}", timesheets.size(), projectId);
        return ResponseEntity.ok(timesheets);
    }

    @GetMapping("/stats/summary")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<TimesheetStatsDTO> getTimesheetStats() {
        log.info("Fetching timesheet stats...");
        TimesheetStatsDTO stats = timesheetService.getTimesheetStats();
        log.info("Timesheet stats fetched successfully: {}", stats);
        return ResponseEntity.ok(stats);
    }
}
