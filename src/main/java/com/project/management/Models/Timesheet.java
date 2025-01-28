package com.project.management.Models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "timesheets")
@Data
@NoArgsConstructor
public class Timesheet {
    @Id
    private String id;
    private String userId;
    private String projectId;
    private LocalDate weekStartDate;
    private Map<DayOfWeek, Integer> dailyHours;
    private String description;
    private TimeSheetStatus status;
    private LocalDateTime submittedAt;
}