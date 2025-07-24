package com.teknokote.ess.core.dao.shifts;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.dto.shifts.ShiftPlanningDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShiftPlanningExecutionDao extends BasicDao<Long, ShiftPlanningExecutionDto> {
    List<ShiftPlanningExecutionDto> findByWorkDay(Long workDayId);

    ShiftPlanningExecutionDto findInProgressExecution(Long stationId);

    ShiftPlanningExecutionDto findByDayAndStatus(Long shiftId, Long shiftPlanningId);

    Optional<ShiftPlanningExecutionDto> findByShiftPlanning(ShiftPlanningDto shiftPlanningDto);

    Optional<ShiftPlanningExecutionDto> findByStation(Long id, Long stationId);

    List<ShiftPlanningExecutionDto> findByStationAndDay(Long stationId, LocalDate day);

    List<ShiftPlanningExecutionDto> findByShiftPlanningIds(List<Long> shiftPlanningIds);

    List<ShiftPlanningExecutionDto> findByWorkDayAndStation(Long stationId, Long workDayExecutionId);

    void deleteForPlannings(List<Long> shiftPlanningIds);
}

