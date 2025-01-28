package com.project.management.controller;

import com.project.management.dto.ApiResponse;
import com.project.management.dto.DashboardMetricsDTO;
import com.project.management.dto.MetricsPublishDTO;
import com.project.management.service.MetricsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/metrics")
@Slf4j
public class MetricsController {
    private final MetricsService metricsService;


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