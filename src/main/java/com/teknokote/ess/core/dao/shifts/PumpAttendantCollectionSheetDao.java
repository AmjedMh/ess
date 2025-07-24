package com.teknokote.ess.core.dao.shifts;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDto;

import java.util.List;

public interface PumpAttendantCollectionSheetDao extends BasicDao<Long, PumpAttendantCollectionSheetDto>
{
    List<PumpAttendantCollectionSheetDto> findByShiftPlanningExecutionId(Long shiftPlanningExecutionId);
}

