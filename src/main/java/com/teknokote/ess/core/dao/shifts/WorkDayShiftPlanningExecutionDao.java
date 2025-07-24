package com.teknokote.ess.core.dao.shifts;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningExecutionDto;

import java.util.List;
import java.util.Optional;


public interface WorkDayShiftPlanningExecutionDao extends BasicDao<Long, WorkDayShiftPlanningExecutionDto>
{
    Optional<WorkDayShiftPlanningExecutionDto> findByWorkDay(Long workDayId);

    void deleteForPlannings(List<Long> workDayIds);
}

