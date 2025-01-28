package com.project.TimeSheetManagement.service;

import com.project.TimeSheetManagement.dto.*;
import com.project.TimeSheetManagement.exception.ResourceNotFoundException;
import com.project.TimeSheetManagement.mapper.ProjectMapper;
import com.project.TimeSheetManagement.mapper.TimesheetMapper;
import com.project.TimeSheetManagement.Models.Project;
import com.project.TimeSheetManagement.Models.ProjectStatus;
import com.project.TimeSheetManagement.Models.Timesheet;
import com.project.TimeSheetManagement.Models.User;
import com.project.TimeSheetManagement.repository.ProjectRepository;
import com.project.TimeSheetManagement.repository.TimesheetRepository;
import com.project.TimeSheetManagement.repository.UserRepository;
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
    private final TimesheetMapper timesheetMapper;

    public ProjectDTO createProject(ProjectDTO projectDTO) {
        Project project = new Project();
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setStartDate(projectDTO.getStartDate());
        project.setEndDate(projectDTO.getEndDate());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setAssignedUsers(new ArrayList<>());
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

        project.getAssignedUsers().addAll(userIds);
        users.forEach(user -> user.getAssignedProjects().add(projectId));

        projectRepository.save(project);
        userRepository.saveAll(users);
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

        // Map assigned users without exposing password
        List<UserDTO> assignedUsers = userRepository.findAllById(project.getAssignedUsers())
                .stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());

        // Get recent timesheets
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

    private List<TimesheetSummaryDTO> getRecentTimesheets(String projectId) {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        List<Timesheet> timesheets = timesheetRepository.findByProjectIdAndWeekStartDateAfter(projectId, oneMonthAgo);

        // Use timesheetMapper for proper DTO mapping
        return timesheets.stream()
                .map(timesheetMapper::toTimesheetSummaryDTO)
                .collect(Collectors.toList());
    }

    private ProjectStatsDTO calculateProjectStats(Project project) {
        List<Timesheet> timesheets = timesheetRepository.findByProjectId(project.getId());
        Set<String> activeUsers = timesheets.stream()
                .filter(t -> t.getWeekStartDate().isAfter(LocalDate.now().minusMonths(1)))
                .map(Timesheet::getUserId)
                .collect(Collectors.toSet());

        Double progress = project.getTotalBudgetHours() > 0
                ? (double) project.getTotalBilledHours() / project.getTotalBudgetHours() * 100
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
}
