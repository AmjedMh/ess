package com.teknokote.ess.core.dao.impl.shifts;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.shifts.WorkDayShiftPlanningExecutionMapper;
import com.teknokote.ess.core.dao.shifts.WorkDayShiftPlanningExecutionDao;
import com.teknokote.ess.core.model.shifts.WorkDayShiftPlanning;
import com.teknokote.ess.core.model.shifts.WorkDayShiftPlanningExecution;
import com.teknokote.ess.core.repository.shifts.WorkDayShiftPlanningExecutionRepository;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningExecutionDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Getter
@Setter
public class WorkDayShiftPlanningExecutionDaoImpl extends JpaGenericDao<Long, WorkDayShiftPlanningExecutionDto, WorkDayShiftPlanningExecution> implements WorkDayShiftPlanningExecutionDao
{
    @Autowired
    private WorkDayShiftPlanningExecutionMapper mapper;
    @Autowired
    private WorkDayShiftPlanningExecutionRepository repository;

    @Override
    protected WorkDayShiftPlanningExecution beforeCreate(WorkDayShiftPlanningExecution workDayShiftPlanning, WorkDayShiftPlanningExecutionDto dto) {
        workDayShiftPlanning.setWorkDayShiftPlanning(getEntityManager().getReference(WorkDayShiftPlanning.class, dto.getWorkDayShiftPlanningId()));
        return super.beforeCreate(workDayShiftPlanning,dto);
    }

    @Override
    public Optional<WorkDayShiftPlanningExecutionDto> findByWorkDay(Long workDayId) {
        return Optional.ofNullable(getMapper().toDto(getRepository().findByWorkDay(workDayId)));
    }

    @Override
    public void deleteForPlannings(List<Long> workDayIds) {
        getRepository().deleteForPlannings(workDayIds);
    }
}
