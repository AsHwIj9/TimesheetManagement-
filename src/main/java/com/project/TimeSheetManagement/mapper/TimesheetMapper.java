package com.project.TimeSheetManagement.mapper;

import com.project.TimeSheetManagement.dto.TimesheetSummaryDTO;
import com.project.TimeSheetManagement.Models.Timesheet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TimesheetMapper {

    // Basic mapping of timesheet fields without custom logic
    TimesheetSummaryDTO toTimesheetSummaryDTO(Timesheet timesheet);
}

