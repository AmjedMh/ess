package com.teknokote.ess.core.dao.impl.shifts;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.core.dao.mappers.shifts.ShiftPlanningMapper;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningDao;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.organization.PumpAttendantTeam;
import com.teknokote.ess.core.model.shifts.Shift;
import com.teknokote.ess.core.model.shifts.ShiftPlanning;
import com.teknokote.ess.core.model.shifts.ShiftRotation;
import com.teknokote.ess.core.model.shifts.WorkDayShiftPlanning;
import com.teknokote.ess.core.repository.shifts.ShiftPlanningRepository;
import com.teknokote.ess.dto.shifts.ShiftPlanningDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Getter
@Setter
public class ShiftPlanningDaoImpl extends JpaGenericDao<Long, ShiftPlanningDto, ShiftPlanning> implements ShiftPlanningDao
{
    @Autowired
    private ShiftPlanningMapper mapper;
    @Autowired
    private ShiftPlanningRepository repository;

    @Override
    public List<ShiftPlanningDto> findAllByStation(Long stationId) {
        return getRepository().findAllByStation(stationId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }
    @Override
    protected ShiftPlanning beforeCreate(ShiftPlanning shiftPlanning, ShiftPlanningDto dto) {
        shiftPlanning.setStation(getEntityManager().getReference(Station.class, dto.getStationId()));
        if (dto.getPumpAttendantTeamId()!=null){
            shiftPlanning.setPumpAttendantTeam(getEntityManager().getReference(PumpAttendantTeam.class, dto.getPumpAttendantTeamId()));
        }
        shiftPlanning.setShift(getEntityManager().getReference(Shift.class, dto.getShiftId()));
        shiftPlanning.setShiftRotation(getEntityManager().getReference(ShiftRotation.class, dto.getShiftRotationId()));
        shiftPlanning.setWorkDayShiftPlanning(getEntityManager().getReference(WorkDayShiftPlanning.class,dto.getWorkDayShiftPlanningId()));
        return super.beforeCreate(shiftPlanning, dto);
    }
    @Override
    protected ShiftPlanning beforeUpdate(ShiftPlanning shiftPlanning, ShiftPlanningDto dto) {
        shiftPlanning.setPumpAttendantTeam(dto.getPumpAttendantTeamId()!=null?getEntityManager().getReference(PumpAttendantTeam.class, shiftPlanning.getPumpAttendantTeamId()):null);
        return super.beforeCreate(shiftPlanning, dto);
    }

    @Override
    public List<ShiftPlanningDto> findByStationAndPeriod(Long stationId, LocalDate date, LocalDate endDate)
    {
        return getRepository().findByStationAndPeriod(stationId,date,endDate).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ShiftPlanningDto>  findByStationAndDay(Long stationId, LocalDate day) {
        return getRepository().findByStationAndDay(stationId,day).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ShiftPlanningDto> findByStationAndRotation(Long stationId, Long shiftRotationId) {
        return getRepository().findByStationAndRotation(stationId,shiftRotationId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    @Override
    public List<Long> findTeamIdsByStationAndShiftRotation(Long stationId, Long shiftRotationId)
    {
        return getRepository().findTeamIdsByStationAndShiftRotation(stationId,shiftRotationId);
    }
    @Override
    public void deletePlanning(List<ShiftPlanningDto> shiftPlannings) {
        try {
            deleteAllByIdInBatch(shiftPlannings.stream().map(ShiftPlanningDto::getId).collect(Collectors.toList()));
        } catch (Exception ex) {
            throw new ServiceValidationException("Could not delete shift planning! Check if an existing shift planning execution is running.");
        }
    }
}
