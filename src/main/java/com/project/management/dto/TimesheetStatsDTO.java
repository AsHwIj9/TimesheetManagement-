package com.project.management.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetStatsDTO {
    @NotNull(message = "Total submitted timesheets cannot be null.")
    private Integer totalSubmittedTimesheets;

    @NotNull(message = "Total billed hours cannot be null.")
    private Integer totalBilledHours;

    private Map<String, Integer> hoursPerProject;
    private Map<String, Integer> hoursPerUser;

    private List<TimesheetSummaryDTO> recentTimesheets;
}