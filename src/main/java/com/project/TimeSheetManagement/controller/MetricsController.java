package com.project.TimeSheetManagement.controller;

import com.project.TimeSheetManagement.dto.ApiResponse;
import com.project.TimeSheetManagement.dto.DashboardMetricsDTO;
import com.project.TimeSheetManagement.dto.MetricsPublishDTO;
import com.project.TimeSheetManagement.service.MetricsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
@Slf4j
public class MetricsController {
    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<DashboardMetricsDTO> getDashboardMetrics() {
        log.info("Fetching dashboard metrics...");
        DashboardMetricsDTO metrics = metricsService.getDashboardMetrics();
        log.info("Dashboard metrics fetched successfully: {}", metrics);
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/publish")
    @PreAuthorize("hasAuthority(@roleProperties.adminRole)")
    public ResponseEntity<ApiResponse<Void>> publishMetrics(@Valid @RequestBody MetricsPublishDTO metricsDTO) {
        log.info("Publishing metrics: {}", metricsDTO);
        metricsService.publishMetrics(metricsDTO);
        log.info("Metrics published successfully.");
        return ResponseEntity.ok(new ApiResponse<>(true, "Metrics published successfully"));
    }

}