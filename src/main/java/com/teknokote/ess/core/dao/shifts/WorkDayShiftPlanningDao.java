package com.teknokote.ess.core.dao.shifts;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.dto.shifts.ShiftPlanningDto;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;

import java.time.LocalDate;
import java.util.List;


public interface WorkDayShiftPlanningDao extends BasicDao<Long, WorkDayShiftPlanningDto>
{

    WorkDayShiftPlanningDto findByStationAndDay(Long stationId, LocalDate day);

    void deleteForPlanning(List<Long> workDayIds);

    boolean hasExecutionsForRotation(Long shiftRotationId);

    List<WorkDayShiftPlanningDto> findByStationAndRotation(Long stationId, Long shiftRotationId);
}

