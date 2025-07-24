package com.teknokote.ess.core.service.impl.shifts;

import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.dao.shifts.WorkDayShiftPlanningDao;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningExecutionService;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningService;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Getter
public class WorkDayShiftPlanningServiceImpl extends GenericCheckedService<Long, WorkDayShiftPlanningDto> implements WorkDayShiftPlanningService
{
    @Autowired
    private ESSValidator<WorkDayShiftPlanningDto> validator;
    @Autowired
    private WorkDayShiftPlanningDao dao;
    @Autowired
    private WorkDayShiftPlanningExecutionService workDayShiftPlanningExecutionService;

    @Override
    public WorkDayShiftPlanningDto findByStationAndDay(Long stationId, LocalDate day) {
        return getDao().findByStationAndDay(stationId,day);
    }

    @Override
    public void deleteForPlannings(List<Long> workDayIds)
    {
        getDao().deleteForPlanning(workDayIds);
    }

    @Override
    public boolean hasExecutionsForRotation(Long shiftRotationId)
    {
        return getDao().hasExecutionsForRotation(shiftRotationId);
    }

    @Override
    public void deleteById(Long id)
    {
        workDayShiftPlanningExecutionService.deleteByWorkDayShiftPlanning(id);
        super.deleteById(id);
    }

    @Override
    public void deleteByStationAndRotation(Long stationId, Long shiftRotationId)
    {
        final List<WorkDayShiftPlanningDto> workDayShiftPlanning = getDao().findByStationAndRotation(stationId,shiftRotationId);
        workDayShiftPlanning.stream().map(WorkDayShiftPlanningDto::getId).forEach(this::deleteById);
    }
}
