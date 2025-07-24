package com.teknokote.ess.core.service.shifts;

import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningExecutionDto;

import java.util.Optional;

public interface WorkDayShiftPlanningExecutionService extends BaseService<Long, WorkDayShiftPlanningExecutionDto>
{

    Optional<WorkDayShiftPlanningExecutionDto> findByWorkDay(Long id);

    /**
     * Supprime les executions d'un work day (id workDay)
     * @param workDayShiftPlanningId
     */
    void deleteByWorkDayShiftPlanning(Long workDayShiftPlanningId);
}
