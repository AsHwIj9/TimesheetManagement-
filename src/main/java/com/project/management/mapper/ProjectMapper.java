package com.project.management.mapper;

import com.project.management.dto.ProjectDTO;
import com.project.management.Models.Project;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface ProjectMapper {


    ProjectDTO toProjectDTO(Project project);
}