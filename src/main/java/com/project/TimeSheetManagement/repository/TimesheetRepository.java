package com.project.TimeSheetManagement.repository;

import com.project.TimeSheetManagement.Models.Timesheet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;



@Repository
public interface TimesheetRepository extends MongoRepository<Timesheet, String> {
    List<Timesheet> findByUserIdAndWeekStartDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    List<Timesheet> findByProjectId(String projectId);
    List<Timesheet> findByProjectIdAndWeekStartDateBetween(String projectId, LocalDate startDate, LocalDate endDate);
    List<Timesheet> findByProjectIdAndWeekStartDateAfter(String projectId, LocalDate startDate);
    List<Timesheet> findByWeekStartDateAfter(LocalDate date);
    boolean existsByUserIdAndProjectIdAndWeekStartDate(String userId, String projectId, LocalDate weekStartDate);
}
