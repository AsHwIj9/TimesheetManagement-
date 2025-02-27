package com.project.management.service;

import com.project.management.dto.*;
import com.project.management.exception.ResourceNotFoundException;
import com.project.management.exception.UserAlreadyExistsException;
import com.project.management.mapper.ProjectMapper;

import com.project.management.Models.Project;
import com.project.management.Models.ProjectStatus;
import com.project.management.Models.Timesheet;
import com.project.management.Models.User;
import com.project.management.repository.ProjectRepository;
import com.project.management.repository.TimesheetRepository;
import com.project.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TimesheetRepository timesheetRepository;
    private final ProjectMapper projectMapper;


    public ProjectDTO createProject(ProjectDTO projectDTO) {
        Project project = new Project();
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setStartDate( projectDTO.getStartDate());
        project.setEndDate(projectDTO.getEndDate());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setAssignedUsers(projectDTO.getAssignedUsers());
        project.setTotalBudgetHours(projectDTO.getTotalBudgetHours());
        project.setTotalBilledHours(0);

        Project savedProject = projectRepository.save(project);
        return projectMapper.toProjectDTO(savedProject);
    }

    public void assignUsersToProject(String projectId, List<String> userIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new ResourceNotFoundException("One or more users not found");
        }


        Set<String> existingUserIds = new HashSet<>(project.getAssignedUsers());


        List<User> newUsers = users.stream()
                .filter(user -> !existingUserIds.contains(user.getId()))
                .toList();

        if (newUsers.isEmpty()) {
            throw new UserAlreadyExistsException("All users are already assigned to this project.");
        }


        List<String> newUserIds = newUsers.stream()
                .map(User::getId)
                .toList();

        project.getAssignedUsers().addAll(newUserIds);


        newUsers.forEach(user -> user.getAssignedProjects().add(project.getId()));

        projectRepository.save(project);
        userRepository.saveAll(newUsers);
    }

    public ProjectDTO updateProjectStatus(String projectId, ProjectStatus newStatus) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));


        if (newStatus == ProjectStatus.COMPLETED || newStatus == ProjectStatus.CANCELLED) {
            project.setStatus(newStatus);
            Project updatedProject = projectRepository.save(project);
            return projectMapper.toProjectDTO(updatedProject);
        } else {
            throw new IllegalArgumentException("Invalid status transition");
        }
    }

    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toProjectDTO)
                .collect(Collectors.toList());
    }

    public List<ProjectStatsDTO> getProjectStats() {
        return projectRepository.findAll().stream()
                .map(this::calculateProjectStats)
                .collect(Collectors.toList());
    }

public ProjectDetailDTO getProjectDetails(String projectId) {
    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));


    List<UserDTO> assignedUsers = userRepository.findAllById(project.getAssignedUsers())
            .stream()
            .map(this::mapToUserDTO)
            .collect(Collectors.toList());

    List<TimesheetSummaryDTO> recentTimesheets = getRecentTimesheets(projectId);

    return new ProjectDetailDTO(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getStatus(),
            project.getStartDate(),
            project.getEndDate(),
            assignedUsers,
            project.getTotalBudgetHours(),
            project.getTotalBilledHours(),
            recentTimesheets
    );
}

    private UserDTO mapToUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null,
                user.getRole(),
                user.getAssignedProjects(),
                user.getCreatedAt()
        );
    }

    private TimesheetSummaryDTO mapToTimesheetSummaryDTO(Timesheet timesheet) {
        if (timesheet == null) {
            return null;
        }

        TimesheetSummaryDTO dto = new TimesheetSummaryDTO();
        dto.setId(timesheet.getId());
        dto.setProjectId(timesheet.getProjectId());
        dto.setWeekStartDate(timesheet.getWeekStartDate());


        dto.setTotalHours(timesheet.getDailyHours() != null ?
                timesheet.getDailyHours().values().stream().mapToInt(Integer::intValue).sum() : 0);

        dto.setStatus(timesheet.getStatus());
        dto.setSubmittedAt(timesheet.getSubmittedAt());

        return dto;
    }

    private ProjectStatsDTO calculateProjectStats(Project project) {
        List<Timesheet> timesheets = timesheetRepository.findByProjectId(project.getId());
        Set<String> activeUsers = timesheets.stream()
                .filter(t -> t.getWeekStartDate().isAfter(LocalDate.now().minusMonths(1)))
                .map(Timesheet::getUserId)
                .collect(Collectors.toSet());

        Double progress = (project.getTotalBudgetHours() != null && project.getTotalBudgetHours() > 0)
                ? ((project.getTotalBilledHours() != null ? project.getTotalBilledHours() : 0) / (double) project.getTotalBudgetHours()) * 100
                : 0.0;

        return new ProjectStatsDTO(
                project.getId(),
                project.getName(),
                activeUsers.size(),
                project.getTotalBilledHours(),
                progress,
                new ArrayList<>(activeUsers)
        );
    }

    private List<TimesheetSummaryDTO> getRecentTimesheets(String projectId) {
        List<Timesheet> recentTimesheets = timesheetRepository.findByProjectId(projectId)
                .stream()
                .sorted(Comparator.comparing(Timesheet::getSubmittedAt).reversed()) // Sort by most recent
                .limit(5) // Get the last 5 timesheets
                .collect(Collectors.toList());

        return recentTimesheets.stream()
                .map(this::mapToTimesheetSummaryDTO)
                .collect(Collectors.toList());
    }
}
