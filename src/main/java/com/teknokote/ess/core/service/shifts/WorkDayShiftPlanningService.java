package com.teknokote.ess.core.service.shifts;

import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;

import java.time.LocalDate;
import java.util.List;

public interface WorkDayShiftPlanningService extends BaseService<Long, WorkDayShiftPlanningDto>
{
    WorkDayShiftPlanningDto findByStationAndDay(Long stationId, LocalDate day) ;

    void deleteForPlannings(List<Long> workDayIds);

    boolean hasExecutionsForRotation(Long shiftRotationId);

    void deleteById(Long id);

    void deleteByStationAndRotation(Long stationId, Long shiftRotationId);
}
