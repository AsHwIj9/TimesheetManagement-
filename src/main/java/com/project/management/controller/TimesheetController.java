package com.project.management.controller;

import com.project.management.dto.*;
import com.project.management.service.TimesheetService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/timesheets")
@Slf4j
public class TimesheetController {
    private final TimesheetService timesheetService;

    public TimesheetController(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority(@roleProperties.userRole)")
    public ResponseEntity<ApiResponse<TimesheetDTO>> submitTimesheet(@Valid @RequestBody TimesheetDTO timesheetDTO) {
        log.info("Submitting timesheet: {}", timesheetDTO);
        TimesheetDTO submitted = timesheetService.submitTimesheet(timesheetDTO);
        log.info("Timesheet submitted successfully: {}", submitted);
        return ResponseEntity.ok(new ApiResponse<>(true, "Timesheet submitted successfully", submitted));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority(@roleProperties.userRole)")
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
