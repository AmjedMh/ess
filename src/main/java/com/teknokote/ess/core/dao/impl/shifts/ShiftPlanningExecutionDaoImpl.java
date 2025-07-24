package com.teknokote.ess.core.dao.impl.shifts;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.shifts.ShiftPlanningExecutionDetailMapper;
import com.teknokote.ess.core.dao.mappers.shifts.ShiftPlanningExecutionMapper;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningExecutionDao;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningExecutionDetailDao;
import com.teknokote.ess.core.model.configuration.Nozzle;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.model.shifts.*;
import com.teknokote.ess.core.repository.shifts.ShiftPlanningExecutionRepository;
import com.teknokote.ess.dto.shifts.ShiftPlanningDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Repository
@Getter
@Setter
public class ShiftPlanningExecutionDaoImpl extends JpaGenericDao<Long, ShiftPlanningExecutionDto, ShiftPlanningExecution> implements ShiftPlanningExecutionDao {
    @Autowired
    private ShiftPlanningExecutionMapper mapper;
    @Autowired
    private ShiftPlanningExecutionDetailMapper executionDetailMapper;
    @Autowired
    private ShiftPlanningExecutionRepository repository;
    @Autowired
    private ShiftPlanningExecutionDetailDao shiftPlanningExecutionDetailDao;

    @Override
    public ShiftPlanningExecutionDto findByDayAndStatus(Long shiftId, Long shiftPlanningId) {
        return getMapper().toDto(getRepository().findByDayAndStatus(shiftId, shiftPlanningId));
    }

    @Override
    public List<ShiftPlanningExecutionDto> findByStationAndDay(Long stationId, LocalDate day) {
        return getRepository().findByStationAndDate(stationId, day).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ShiftPlanningExecutionDto> findByShiftPlanningIds(List<Long> shiftPlanningIds) {
        return getRepository().findByShiftPlanningIds(shiftPlanningIds).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ShiftPlanningExecutionDto> findByWorkDayAndStation(Long stationId, Long workDayId) {
        return getRepository().findByWorkDayAndStation(stationId, workDayId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ShiftPlanningExecutionDto> findByWorkDay(Long workDayId) {
        return getRepository().findByWorkDay(workDayId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    @Override
    public ShiftPlanningExecutionDto findInProgressExecution(Long stationId) {
        return getMapper().toDto(getRepository().findInProgressExecution(stationId, ShiftExecutionStatus.IN_PROGRESS));
    }

    @Override
    public Optional<ShiftPlanningExecutionDto> findByShiftPlanning(ShiftPlanningDto shiftPlanningDto) {
        return getRepository().findByShiftPlanningId(shiftPlanningDto.getId()).map(getMapper()::toDto);
    }

    @Override
    public Optional<ShiftPlanningExecutionDto> findByStation(Long id, Long stationId) {
        return getRepository().findByStation(id, stationId).map(getMapper()::toDto);
    }

    @Override
    protected ShiftPlanningExecution beforeCreate(ShiftPlanningExecution shiftPlanningExecution, ShiftPlanningExecutionDto dto) {
        shiftPlanningExecution.setShiftPlanning(getEntityManager().getReference(ShiftPlanning.class, dto.getShiftPlanningId()));
        shiftPlanningExecution.setShift(getEntityManager().getReference(Shift.class, dto.getShiftId()));
        shiftPlanningExecution.setWorkDayShiftPlanningExecution(getEntityManager().getReference(WorkDayShiftPlanningExecution.class, dto.getWorkDayShiftPlanningExecutionId()));
        return super.beforeCreate(beforeUpdateOnDetails(shiftPlanningExecution), dto);
    }

    @Override
    protected ShiftPlanningExecution beforeUpdate(ShiftPlanningExecution shiftPlanningExecution, ShiftPlanningExecutionDto dto) {
        if (Objects.nonNull(dto.getShiftPlanningId())) {
            shiftPlanningExecution.setShiftPlanning(getEntityManager().getReference(ShiftPlanning.class, dto.getShiftPlanningId()));
        }
        if (Objects.nonNull(dto.getShiftId())) {
            shiftPlanningExecution.setShift(getEntityManager().getReference(Shift.class, dto.getShiftId()));
        }
        for (PumpAttendantCollectionSheet collectionSheet : shiftPlanningExecution.getPumpAttendantCollectionSheets()) {
            collectionSheet.setPumpAttendant(getEntityManager().getReference(PumpAttendant.class, collectionSheet.getPumpAttendantId()));
            collectionSheet.setShiftPlanningExecution(shiftPlanningExecution);
        }
        return super.beforeUpdate(beforeUpdateOnDetails(shiftPlanningExecution), dto);
    }

    /**
     * I bring dependencies here with the entity itself because the copy was already done from the dto to the entity.
     */
    private ShiftPlanningExecution beforeUpdateOnDetails(ShiftPlanningExecution shiftPlanningExecution) {
        shiftPlanningExecution.getShiftPlanningExecutionDetail().forEach(detail -> {
            detail.setShiftPlanningExecution(shiftPlanningExecution);
            if (Objects.nonNull(detail.getPumpId())) {
                detail.setPump(getEntityManager().getReference(Pump.class, detail.getPumpId()));
            }
            if (Objects.nonNull(detail.getNozzleId())) {
                detail.setNozzle(getEntityManager().getReference(Nozzle.class, detail.getNozzleId()));
            }
            if (Objects.nonNull(detail.getPumpAttendantId())) {
                detail.setPumpAttendant(getEntityManager().getReference(PumpAttendant.class, detail.getPumpAttendantId()));
            }
        });
        return shiftPlanningExecution;
    }

    @Override
    public void deleteForPlannings(List<Long> shiftPlanningIds) {
        // Delete from details
        shiftPlanningExecutionDetailDao.deleteForPlannings(shiftPlanningIds);
        // Delete from executions
        getRepository().deleteForPlannings(shiftPlanningIds);
    }
}
