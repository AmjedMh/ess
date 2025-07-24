package com.teknokote.ess.core.service.impl.shifts;

import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.dao.shifts.WorkDayShiftPlanningExecutionDao;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningExecutionService;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningExecutionDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Getter
public class WorkDayShiftPlanningExecutionServiceImpl extends GenericCheckedService<Long, WorkDayShiftPlanningExecutionDto> implements WorkDayShiftPlanningExecutionService
{
    @Autowired
    private ESSValidator<WorkDayShiftPlanningExecutionDto> validator;
    @Autowired
    private WorkDayShiftPlanningExecutionDao dao;

    @Override
    public Optional<WorkDayShiftPlanningExecutionDto> findByWorkDay(Long workDayId) {
        return getDao().findByWorkDay(workDayId);
    }

    @Override
    public void deleteByWorkDayShiftPlanning(Long workDayShiftPlanningId)
    {
        final Optional<WorkDayShiftPlanningExecutionDto> optWorkDayExec = findByWorkDay(workDayShiftPlanningId);
        optWorkDayExec.ifPresent(workDayShiftPlanningExecutionDto -> deleteById(workDayShiftPlanningExecutionDto.getId()));
    }
}
