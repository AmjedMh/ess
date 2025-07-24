package com.teknokote.ess.core.dao.impl.shifts;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.shifts.WorkDayShiftPlanningMapper;
import com.teknokote.ess.core.dao.shifts.WorkDayShiftPlanningDao;
import com.teknokote.ess.core.dao.shifts.WorkDayShiftPlanningExecutionDao;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.shifts.ShiftRotation;
import com.teknokote.ess.core.model.shifts.WorkDayShiftPlanning;
import com.teknokote.ess.core.repository.shifts.WorkDayShiftPlanningRepository;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@Getter
@Setter
public class WorkDayShiftPlanningDaoImpl extends JpaGenericDao<Long, WorkDayShiftPlanningDto, WorkDayShiftPlanning> implements WorkDayShiftPlanningDao
{
    @Autowired
    private WorkDayShiftPlanningMapper mapper;
    @Autowired
    private WorkDayShiftPlanningRepository repository;
    @Autowired
    private WorkDayShiftPlanningExecutionDao workDayShiftPlanningExecutionDao;
    @Override
    protected WorkDayShiftPlanning beforeCreate(WorkDayShiftPlanning workDayShiftPlanning, WorkDayShiftPlanningDto dto) {
        workDayShiftPlanning.setStation(getEntityManager().getReference(Station.class, dto.getStationId()));
        workDayShiftPlanning.setShiftRotation(getEntityManager().getReference(ShiftRotation.class, dto.getShiftRotationId()));
        return super.beforeCreate(workDayShiftPlanning,dto);
    }
    @Override
    public WorkDayShiftPlanningDto findByStationAndDay(Long stationId, LocalDate day) {
        return getMapper().toDto(getRepository().findByStationAndDay(stationId,day));
    }

    @Override
    public void deleteForPlanning(List<Long> workDayIds) {
        workDayShiftPlanningExecutionDao.deleteForPlannings(workDayIds);
        getRepository().deleteForPlannings(workDayIds);
    }

    @Override
    public List<WorkDayShiftPlanningDto> findByStationAndRotation(Long stationId, Long shiftRotationId)
    {
        return getRepository().findByStationAndRotation(stationId,shiftRotationId).stream().map(getMapper()::toDto).toList();
    }

    @Override
    public boolean hasExecutionsForRotation(Long shiftRotationId)
    {
        return getRepository().hasExecutionsForRotation(shiftRotationId) >0;
    }
}
