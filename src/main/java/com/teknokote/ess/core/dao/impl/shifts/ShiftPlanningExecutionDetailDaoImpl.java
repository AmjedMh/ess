package com.teknokote.ess.core.dao.impl.shifts;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.shifts.ShiftPlanningExecutionDetailMapper;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningExecutionDetailDao;
import com.teknokote.ess.core.model.configuration.Nozzle;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.model.shifts.ShiftPlanningExecution;
import com.teknokote.ess.core.model.shifts.ShiftPlanningExecutionDetail;
import com.teknokote.ess.core.repository.shifts.ShiftPlanningExecutionDetailRepository;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDetailDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Getter
@Setter
public class ShiftPlanningExecutionDetailDaoImpl extends JpaGenericDao<Long, ShiftPlanningExecutionDetailDto, ShiftPlanningExecutionDetail> implements ShiftPlanningExecutionDetailDao
{
    @Autowired
    private ShiftPlanningExecutionDetailMapper mapper;
    @Autowired
    private ShiftPlanningExecutionDetailRepository repository;
    @Override
    protected ShiftPlanningExecutionDetail beforeCreate(ShiftPlanningExecutionDetail shiftPlanningExecutionDetail, ShiftPlanningExecutionDetailDto dto) {
        shiftPlanningExecutionDetail.setShiftPlanningExecution(getEntityManager().getReference(ShiftPlanningExecution.class,dto.getShiftPlanningExecutionId()));
        shiftPlanningExecutionDetail.setPump(getEntityManager().getReference(Pump.class,dto.getPumpId()));
        shiftPlanningExecutionDetail.setNozzle(getEntityManager().getReference(Nozzle.class,dto.getNozzleId()));
        shiftPlanningExecutionDetail.setPumpAttendant(getEntityManager().getReference(PumpAttendant.class,dto.getPumpAttendantId()));
        return super.beforeCreate(shiftPlanningExecutionDetail, dto);
    }

    @Override
    protected ShiftPlanningExecutionDetail beforeUpdate(ShiftPlanningExecutionDetail entity, ShiftPlanningExecutionDetailDto dto)
    {
        entity.setPump(getEntityManager().getReference(Pump.class,dto.getPumpId()));
        entity.setNozzle(getEntityManager().getReference(Nozzle.class,dto.getNozzleId()));
        entity.setPumpAttendant(getEntityManager().getReference(PumpAttendant.class,dto.getPumpAttendantId()));
        entity.setShiftPlanningExecution(getEntityManager().getReference(ShiftPlanningExecution.class,entity.getShiftPlanningExecutionId()));
        return super.beforeUpdate(entity, dto);
    }

    @Override
    public List<ShiftPlanningExecutionDetailDto> findByShiftPlanningExecutionIdAndPumpAttendantId(Long shiftPlanningExecutionId, Long pumpAttendantId) {
        return getRepository().findByShiftPlanningExecutionIdAndPumpAttendantId(shiftPlanningExecutionId,pumpAttendantId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }
    @Override
    public List<ShiftPlanningExecutionDetailDto> findByShiftPlanningExecutionDetailIdAndPumpAttendantIdAndPumpId(Long shiftPlanningExecutionDetailId, Long pumpAttendantId, Long pumpId) {
        return getRepository().findByShiftPlanningExecutionDetailIdAndPumpAttendantIdAndPumpId(shiftPlanningExecutionDetailId,pumpAttendantId,pumpId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }
    @Override
    public ShiftPlanningExecutionDetailDto findByStation(Long id, Long stationId) {
        return  getMapper().toDto(getRepository().findByStation(id,stationId));
    }

    @Override
    public void deleteForPlannings(List<Long> shiftPlanningIds)
    {
        getRepository().deleteForPlannings(shiftPlanningIds);
    }
}
