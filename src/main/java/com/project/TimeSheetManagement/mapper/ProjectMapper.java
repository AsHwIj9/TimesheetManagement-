package com.project.TimeSheetManagement.mapper;

import com.project.TimeSheetManagement.dto.ProjectDTO;
import com.project.TimeSheetManagement.Models.Project;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface ProjectMapper {


    ProjectDTO toProjectDTO(Project project);
}