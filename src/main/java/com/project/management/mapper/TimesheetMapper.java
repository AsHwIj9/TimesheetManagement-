package com.project.management.mapper;

import com.project.management.dto.TimesheetSummaryDTO;
import com.project.management.Models.Timesheet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TimesheetMapper {

    // Basic mapping of timesheet fields without custom logic
    TimesheetSummaryDTO toTimesheetSummaryDTO(Timesheet timesheet);
}

