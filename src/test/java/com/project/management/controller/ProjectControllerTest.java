package com.project.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.management.Models.ProjectStatus;
import com.project.management.dto.*;
import com.project.management.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProjectDTO projectDTO;
    private final String testProjectId = "1";

    @BeforeEach
    void setUp() {
        projectDTO = new ProjectDTO();
        projectDTO.setId(testProjectId);
        projectDTO.setName("Test Project");
        projectDTO.setStatus(ProjectStatus.ACTIVE);
        projectDTO.setDescription("Test Description");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createProject_Success() throws Exception {
        when(projectService.createProject(any(ProjectDTO.class))).thenReturn(projectDTO);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Project created successfully"))
                .andExpect(jsonPath("$.data.id").value(testProjectId))
                .andExpect(jsonPath("$.data.name").value("Test Project"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllProjects_Success() throws Exception {
        List<ProjectDTO> projects = Collections.singletonList(projectDTO);
        when(projectService.getAllProjects()).thenReturn(projects);

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testProjectId))
                .andExpect(jsonPath("$[0].name").value("Test Project"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void assignUsersToProject_Success() throws Exception {
        ProjectAssignmentDTO assignmentDTO = new ProjectAssignmentDTO();
        assignmentDTO.setProjectId(testProjectId);
        assignmentDTO.setUserIds(List.of("user1", "user2"));

        mockMvc.perform(post("/api/projects/{projectId}/users", testProjectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Users assigned successfully"));

        verify(projectService).assignUsersToProject(testProjectId, assignmentDTO.getUserIds());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getProjectDetails_Success() throws Exception {
        ProjectDetailDTO projectDetailDTO = new ProjectDetailDTO();
        projectDetailDTO.setProjectId(testProjectId);
        projectDetailDTO.setProjectName("Test Project");
        projectDetailDTO.setDescription("Test Description");
        projectDetailDTO.setStatus(ProjectStatus.ACTIVE);

        when(projectService.getProjectDetails(testProjectId)).thenReturn(projectDetailDTO);

        mockMvc.perform(get("/api/projects/{projectId}", testProjectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(testProjectId))
                .andExpect(jsonPath("$.projectName").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateProjectStatus_Success() throws Exception {
        projectDTO.setStatus(ProjectStatus.COMPLETED);
        when(projectService.updateProjectStatus(testProjectId, ProjectStatus.COMPLETED))
                .thenReturn(projectDTO);

        mockMvc.perform(patch("/api/projects/{projectId}/status", testProjectId)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Project status updated successfully"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createProject_ValidationFailure() throws Exception {
        ProjectDTO invalidProject = new ProjectDTO();
        // Leave required fields empty to trigger validation

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProject)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accessWithoutAuthentication_Failure() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void accessWithInsufficientRole_Failure() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isForbidden());
    }
}