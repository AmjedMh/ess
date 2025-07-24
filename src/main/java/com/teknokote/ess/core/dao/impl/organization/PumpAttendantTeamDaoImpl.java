package com.teknokote.ess.core.dao.impl.organization;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.organization.PumpAttendantTeamMapper;
import com.teknokote.ess.core.dao.organization.PumpAttendantTeamDao;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.model.organization.PumpAttendantTeam;
import com.teknokote.ess.core.model.shifts.AffectedPumpAttendant;
import com.teknokote.ess.core.model.shifts.ShiftRotation;
import com.teknokote.ess.core.repository.organization.PumpAttendantTeamRepository;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Getter
@Setter
public class PumpAttendantTeamDaoImpl extends JpaGenericDao<Long, PumpAttendantTeamDto, PumpAttendantTeam> implements PumpAttendantTeamDao {
    @Autowired
    private PumpAttendantTeamMapper mapper;
    @Autowired
    private PumpAttendantTeamRepository repository;

    @Override
    public List<PumpAttendantTeamDto> findAllByStation(Long stationId) {
        return getRepository().findAllByStation(stationId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    @Override
    protected PumpAttendantTeam beforeCreate(PumpAttendantTeam pumpAttendantTeam, PumpAttendantTeamDto dto) {
        pumpAttendantTeam.setStation(getEntityManager().getReference(Station.class, dto.getStationId()));
        PumpAttendantTeam savedPumpAttendantTeam = super.beforeCreate(pumpAttendantTeam, dto);
        if (Objects.nonNull(dto.getShiftRotationId())){
            pumpAttendantTeam.setShiftRotation(getEntityManager().getReference(ShiftRotation.class,dto.getShiftRotationId()));
        }
        // Set the PumpAttendantTeam in each AffectedPumpAttendant
        Set<AffectedPumpAttendant> affectedPumpAttendants = pumpAttendantTeam.getAffectedPumpAttendant();
        if (affectedPumpAttendants != null) {
            affectedPumpAttendants.forEach(affectedPumpAttendant -> {
                affectedPumpAttendant.setPumpAttendantTeam(savedPumpAttendantTeam);
                affectedPumpAttendant.setPump(getEntityManager().getReference(Pump.class, affectedPumpAttendant.getPumpId()));
                affectedPumpAttendant.setPumpAttendant(getEntityManager().getReference(PumpAttendant.class, affectedPumpAttendant.getPumpAttendantId()));
            });
        }
        pumpAttendantTeam.setAffectedPumpAttendant(affectedPumpAttendants);
        return pumpAttendantTeam;
    }

    @Override
    protected PumpAttendantTeam beforeUpdate(PumpAttendantTeam pumpAttendantTeam, PumpAttendantTeamDto dto) {

        pumpAttendantTeam.setStation(getEntityManager().getReference(Station.class, pumpAttendantTeam.getStationId()));
        if (Objects.nonNull(dto.getAffectedPumpAttendant())) {
            pumpAttendantTeam.getAffectedPumpAttendant().forEach(affectedPumpAttendant -> {
                affectedPumpAttendant.setPumpAttendantTeam(pumpAttendantTeam);
                affectedPumpAttendant.setPumpAttendant(getEntityManager().getReference(PumpAttendant.class, affectedPumpAttendant.getPumpAttendantId()));
                affectedPumpAttendant.setPump(getEntityManager().getReference(Pump.class, affectedPumpAttendant.getPumpId()));
            });
        }
        return super.beforeUpdate(pumpAttendantTeam, dto);
    }

    @Override
    public PumpAttendantTeamDto findByShiftIdAndShiftRotationId(Long shiftId, Long shiftRotationId) {
        return getMapper().toDto(getRepository().findByShiftIdAndShiftRotationId(shiftId, shiftRotationId));
    }

    @Override
    public Optional<PumpAttendantTeamDto> findByRotationAndName(Long stationId, Long shiftRotationId, String name) {
        return Optional.ofNullable(getMapper().toDto(getRepository().findByRotationAndName(stationId,shiftRotationId,name).orElse(null)));    }

    @Override
    public List<PumpAttendantTeamDto> findByPumpAttendant(Long pumpAttendantId) {
        return getRepository().findByPumpAttendant(pumpAttendantId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    @Override
    public List<PumpAttendantTeamDto> findTeamsByRotationAndPumpAttendant(Long shiftRotationId, Long pumpAttendantId) {
        return getRepository().findTeamsByRotationAndPumpAttendant(shiftRotationId,pumpAttendantId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    @Override
    public List<PumpAttendantTeamDto> findByStationAndRotation(Long stationId, Long shiftRotationId) {
        return getRepository().findByStationAndRotation(stationId,shiftRotationId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    @Override
    public void deleteForStationAndRotation(Long stationId, Long shiftRotationId)
    {
        getRepository().findByStationAndRotation(stationId,shiftRotationId).stream().map(PumpAttendantTeam::getId).toList().forEach(this::deleteById);
    }
}
