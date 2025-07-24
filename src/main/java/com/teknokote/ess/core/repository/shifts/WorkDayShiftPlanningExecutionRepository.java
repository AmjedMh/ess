package com.teknokote.ess.core.repository.shifts;

import com.teknokote.ess.core.model.shifts.WorkDayShiftPlanningExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkDayShiftPlanningExecutionRepository extends JpaRepository<WorkDayShiftPlanningExecution, Long>
{
    @Query("SELECT wp FROM WorkDayShiftPlanningExecution wp WHERE wp.workDayShiftPlanningId = :workDayId ")
    WorkDayShiftPlanningExecution findByWorkDay(Long workDayId);
    @Modifying
    @Query("DELETE FROM WorkDayShiftPlanningExecution sped WHERE sped.workDayShiftPlanning.id IN " +
            "(SELECT spe.id FROM WorkDayShiftPlanning spe WHERE spe.id IN :workDayIds)")
    void deleteForPlannings(List<Long> workDayIds);

}
