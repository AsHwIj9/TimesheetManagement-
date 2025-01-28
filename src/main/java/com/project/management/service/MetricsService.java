package com.project.management.service;

import com.project.management.dto.DashboardMetricsDTO;
import com.project.management.dto.MetricsPublishDTO;
import com.project.management.dto.ProjectStatsDTO;
import com.project.management.dto.UserWeeklyStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MetricsService {

    private final ProjectService projectService;
    private final UserService userService;

    public DashboardMetricsDTO getDashboardMetrics() {
        List<ProjectStatsDTO> allProjectStats = projectService.getProjectStats();
        List<UserWeeklyStatsDTO> userStats = userService.getUsersWeeklyStats(
                LocalDate.now().minusWeeks(1),
                LocalDate.now()
        );

        return new DashboardMetricsDTO(
                (int) allProjectStats.stream().filter(p -> p.getActiveResourceCount() > 0).count(),
                userStats.size(),
                calculateTotalBilledHours(allProjectStats),
                calculateAverageUtilization(userStats),
                getTopProjects(allProjectStats),
                getTopResources(userStats)
        );
    }



    public void publishMetrics(MetricsPublishDTO metricsDTO) {
        // for publishing metrics to external systems or generating reports
        //  includes:
        //  Generating PDF reports
        //  Sending email notifications
        //  Storing metrics history
        //  Updating external dashboards
    }
    private Double calculateAverageUtilization(List<UserWeeklyStatsDTO> userStats) {
        return userStats.stream()
                .mapToDouble(UserWeeklyStatsDTO::getUtilizationPercentage)
                .average()
                .orElse(0.0);
    }


    private List<ProjectStatsDTO> getTopProjects(List<ProjectStatsDTO> allProjects) {
        return allProjects.stream()
                .sorted(Comparator.comparing(ProjectStatsDTO::getTotalBilledHours).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<UserWeeklyStatsDTO> getTopResources(List<UserWeeklyStatsDTO> allUsers) {
        return allUsers.stream()
                .sorted(Comparator.comparing(UserWeeklyStatsDTO::getUtilizationPercentage).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }
    private Integer calculateTotalBilledHours(List<ProjectStatsDTO> allProjectStats) {
        return allProjectStats.stream()
                .mapToInt(ProjectStatsDTO::getTotalBilledHours)
                .sum();
    }
}
