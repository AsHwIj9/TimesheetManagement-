package com.project.TimeSheetManagement.Models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "projects")
@Data
@NoArgsConstructor
public class Project {
    @Id
    private String id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ProjectStatus status;
    private List<String> assignedUsers;
    private Integer totalBudgetHours;
    private Integer totalBilledHours;
}

