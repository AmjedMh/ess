package com.teknokote.ess.core.service.shifts;

import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningDto;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;

public interface ShiftPlanningService extends BaseService<Long, ShiftPlanningDto>
{
   List<ShiftPlanningDto> resetShiftPlanning(Long stationId, LocalDate month,HttpServletRequest request);
   void deleteShiftPlanning(Long stationId, LocalDate month);
   List<ShiftPlanningDto> generatePlanning(Long stationId, LocalDate month, HttpServletRequest request);
   List<ShiftPlanningDto> generate(Long stationId, LocalDate startDate, LocalDate endDate, Integer numberOffDays);
   List<DynamicShiftPlanningDto> getDynamicShiftPlannig(Long stationId);

   DynamicShiftPlanningDto mapToDynamicDto(ShiftPlanningDto shiftPlanningDto);

   List<ShiftPlanningDto> findByStationAndMonth(Long stationId, LocalDate month);

   List<ShiftPlanningDto> findByStationAndDay(Long stationId, LocalDate day);

   List<PumpAttendantTeamDto> findTeamsByStationAndShiftRotation(Long stationId, Long shiftRotationId);

   List<ShiftPlanningDto> updateList(List<ShiftPlanningDto> dto);
   void markAsExecuting(Long shiftPlanningId);
}
