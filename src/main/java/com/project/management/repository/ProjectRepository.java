package com.project.management.repository;

import com.project.management.Models.Project;
import com.project.management.Models.ProjectStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
    Optional<Project> findByName(String name);
    List<Project> findByAssignedUsers(List<String> assignedUsers);
    List<Project> findByStatus(ProjectStatus status);
    List<Project> findByStatusAndAssignedUsersContaining(ProjectStatus status, String userId);
}
