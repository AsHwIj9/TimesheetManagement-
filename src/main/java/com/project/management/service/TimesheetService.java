package com.project.management.service;

import com.project.management.dto.TimesheetDTO;
import com.project.management.dto.TimesheetResponseDTO;
import com.project.management.dto.TimesheetStatsDTO;
import com.project.management.dto.TimesheetSummaryDTO;
import com.project.management.Models.Project;
import com.project.management.Models.TimeSheetStatus;
import com.project.management.Models.Timesheet;
import com.project.management.Models.User;
import com.project.management.exception.ResourceNotFoundException;
import com.project.management.repository.ProjectRepository;
import com.project.management.repository.TimesheetRepository;
import com.project.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TimesheetDTO submitTimesheet(TimesheetDTO timesheetDTO) {
        validateTimesheetSubmission(timesheetDTO);

        Timesheet timesheet = new Timesheet();
        timesheet.setUserId(timesheetDTO.getUserId());
        timesheet.setProjectId(timesheetDTO.getProjectId());
        timesheet.setWeekStartDate(timesheetDTO.getWeekStartDate());
        timesheet.setDailyHours(timesheetDTO.getDailyHours());
        timesheet.setDescription(timesheetDTO.getDescription());
        timesheet.setStatus(TimeSheetStatus.SUBMITTED);
        timesheet.setSubmittedAt(LocalDateTime.now());

        updateProjectBilledHours(timesheetDTO);

        Timesheet savedTimesheet = timesheetRepository.save(timesheet);
        return mapToDTO(savedTimesheet);
    }

    public TimesheetResponseDTO getTimesheetById(String timesheetId) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet not found with ID: " + timesheetId));


        return new TimesheetResponseDTO(
                timesheet.getId(),
                timesheet.getUserId(),
                timesheet.getProjectId(),
                timesheet.getWeekStartDate(),
                timesheet.getDailyHours(),
                timesheet.getDescription(),
                timesheet.getStatus(),
                timesheet.getSubmittedAt()
        );
    }


    public TimesheetDTO approveTimesheet(String timesheetId) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));


        if (timesheet.getStatus() != TimeSheetStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted timesheets can be approved");
        }

        timesheet.setStatus(TimeSheetStatus.APPROVED);
        Timesheet savedTimesheet = timesheetRepository.save(timesheet);
        return mapToDTO(savedTimesheet);
    }

    public TimesheetDTO rejectTimesheet(String timesheetId, String rejectionReason) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));


        if (timesheet.getStatus() != TimeSheetStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted timesheets can be rejected");
        }

        timesheet.setStatus(TimeSheetStatus.REJECTED);
        timesheet.setDescription(timesheet.getDescription() + "\nRejection reason: " + rejectionReason);
        Timesheet savedTimesheet = timesheetRepository.save(timesheet);
        return mapToDTO(savedTimesheet);
    }

    public List<TimesheetSummaryDTO> getUserTimesheets(String userId, LocalDate startDate, LocalDate endDate) {
        List<Timesheet> timesheets = timesheetRepository
                .findByUserIdAndWeekStartDateBetween(userId, startDate, endDate);
        return timesheets.stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    public List<TimesheetSummaryDTO> getProjectTimesheets(String projectId, LocalDate startDate, LocalDate endDate) {
        List<Timesheet> timesheets = timesheetRepository
                .findByProjectIdAndWeekStartDateBetween(projectId, startDate, endDate);
        return timesheets.stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    public TimesheetStatsDTO getTimesheetStats() {
        List<Timesheet> allTimesheets = timesheetRepository.findAll();

        Map<String, Integer> hoursPerProject = new HashMap<>();
        Map<String, Integer> hoursPerUser = new HashMap<>();
        Integer totalBilledHours = 0;

        for (Timesheet timesheet : allTimesheets) {
            Integer hours = calculateTotalHours(timesheet.getDailyHours());
            totalBilledHours += hours;

            hoursPerProject.merge(timesheet.getProjectId(), hours, Integer::sum);
            hoursPerUser.merge(timesheet.getUserId(), hours, Integer::sum);
        }

        List<TimesheetSummaryDTO> recentTimesheets = getRecentTimesheets();

        return new TimesheetStatsDTO(
                allTimesheets.size(),
                totalBilledHours,
                hoursPerProject,
                hoursPerUser,
                recentTimesheets
        );
    }

    private void validateTimesheetSubmission(TimesheetDTO timesheetDTO) {

        Optional<Project> projectOptional = projectRepository.findById(timesheetDTO.getProjectId());
        Project project = projectOptional.orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Optional<User> userOptional = userRepository.findById(timesheetDTO.getUserId());
        User user = userOptional.orElseThrow(() -> new ResourceNotFoundException("User not found"));


        if (!project.getAssignedUsers().contains(user.getId())) {
            throw new IllegalArgumentException("User is not assigned to this project");
        }


        if (timesheetRepository.existsByUserIdAndProjectIdAndWeekStartDate(
                timesheetDTO.getUserId(),
                timesheetDTO.getProjectId(),
                timesheetDTO.getWeekStartDate())) {
            throw new IllegalStateException("Timesheet already exists for this week");
        }
    }

    private TimesheetDTO mapToDTO(Timesheet timesheet) {
        return new TimesheetDTO(
                timesheet.getId(),
                timesheet.getUserId(),
                timesheet.getProjectId(),
                timesheet.getWeekStartDate(),
                timesheet.getDailyHours(),
                timesheet.getDescription(),
                timesheet.getStatus(),
                timesheet.getSubmittedAt()
        );
    }

    private TimesheetSummaryDTO mapToSummaryDTO(Timesheet timesheet) {
        return new TimesheetSummaryDTO(
                timesheet.getId(),
                timesheet.getProjectId(),
                timesheet.getWeekStartDate(),
                calculateTotalHours(timesheet.getDailyHours()),
                timesheet.getStatus(),
                timesheet.getSubmittedAt()
        );
    }

    private List<TimesheetSummaryDTO> getRecentTimesheets() {
        LocalDate oneWeekAgo = LocalDate.now().minusDays(7);
        List<Timesheet> recentTimesheets = timesheetRepository.findByWeekStartDateAfter(oneWeekAgo);
        return recentTimesheets.stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    private Integer calculateTotalHours(Map<DayOfWeek, Integer> dailyHours) {
        return dailyHours.values().stream().mapToInt(Integer::intValue).sum();
    }

    private void updateProjectBilledHours(TimesheetDTO timesheetDTO) {
        Optional<Project> projectOptional = projectRepository.findById(timesheetDTO.getProjectId());
        Project project = projectOptional.orElseThrow(() -> new IllegalArgumentException("Project not found"));


        Integer additionalHours = calculateTotalHours(timesheetDTO.getDailyHours());
        project.setTotalBilledHours(project.getTotalBilledHours() + additionalHours);

        projectRepository.save(project);
    }
}
