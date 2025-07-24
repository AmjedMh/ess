package com.teknokote.ess.core.dao.shifts;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDetailDto;

import java.util.List;

public interface ShiftPlanningExecutionDetailDao extends BasicDao<Long, ShiftPlanningExecutionDetailDto>
{
    List<ShiftPlanningExecutionDetailDto> findByShiftPlanningExecutionIdAndPumpAttendantId(Long shiftPlanningExecutionId, Long pumpAttendantId);
    List<ShiftPlanningExecutionDetailDto> findByShiftPlanningExecutionDetailIdAndPumpAttendantIdAndPumpId(Long shiftPlanningExecutionDetailId, Long pumpAttendantId,Long pumpId);
    ShiftPlanningExecutionDetailDto findByStation(Long id,Long stationId);

   void deleteForPlannings(List<Long> shiftPlanningIds);
}

