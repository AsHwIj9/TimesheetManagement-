package com.project.management.controller;

import com.project.management.dto.DashboardMetricsDTO;
import com.project.management.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetricsService metricsService;

    private DashboardMetricsDTO mockMetrics;

    @BeforeEach
    void setUp() {
        // Create mock data with correct fields
        mockMetrics = new DashboardMetricsDTO(
                5, 10, 20, 75.0, List.of(), List.of()
        );

        when(metricsService.getDashboardMetrics()).thenReturn(mockMetrics);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testGetDashboardMetrics_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/metrics/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeProjects").value(5))
                .andExpect(jsonPath("$.totalResources").value(10))
                .andExpect(jsonPath("$.totalBilledHours").value(20))
                .andExpect(jsonPath("$.averageUtilization").value(75.0));
    }
    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void testGetDashboardMetrics_AsUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/metrics/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetDashboardMetrics_Unauthenticated_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/metrics/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}