package com.project.management.mapper;

import com.project.management.dto.ProjectDTO;
import com.project.management.Models.Project;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "assignedUsers", source = "assignedUsers")
    @Mapping(target = "totalBudgetHours", source = "totalBudgetHours")
    @Mapping(target = "totalBilledHours", source = "totalBilledHours")
    ProjectDTO toProjectDTO(Project project);

    @InheritInverseConfiguration
    Project toProject(ProjectDTO projectDTO);
}
