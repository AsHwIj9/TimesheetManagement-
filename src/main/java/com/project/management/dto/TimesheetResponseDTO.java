package com.project.management.dto;

import com.project.management.Models.TimeSheetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetResponseDTO {
    private String id;
    private String userId;
    private String projectId;
    private LocalDate weekStartDate;
    private Map<DayOfWeek, Integer> dailyHours;
    private String description;
    private TimeSheetStatus status;
    private LocalDateTime submittedAt;
}