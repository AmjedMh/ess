package com.teknokote.ess.core.dao.shifts;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.dto.shifts.ShiftPlanningDto;

import java.time.LocalDate;
import java.util.List;

public interface ShiftPlanningDao extends BasicDao<Long, ShiftPlanningDto>
{
   List<ShiftPlanningDto> findAllByStation(Long stationId);

   List<ShiftPlanningDto> findByStationAndPeriod(Long stationId, LocalDate startDate, LocalDate endDate);
   List<ShiftPlanningDto> findByStationAndDay(Long stationId, LocalDate day);
   List<ShiftPlanningDto> findByStationAndRotation(Long stationId, Long shiftRotationId);
   void deletePlanning(List<ShiftPlanningDto> shiftPlannings);
   List<Long> findTeamIdsByStationAndShiftRotation(Long stationId, Long shiftRotationId);
}

