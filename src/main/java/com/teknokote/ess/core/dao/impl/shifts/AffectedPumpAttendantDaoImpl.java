package com.teknokote.ess.core.dao.impl.shifts;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.shifts.AffectedPumpAttendantMapper;
import com.teknokote.ess.core.dao.shifts.AffectedPumpAttendantDao;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.model.organization.PumpAttendantTeam;
import com.teknokote.ess.core.model.shifts.AffectedPumpAttendant;
import com.teknokote.ess.core.repository.shifts.AffectedPumpAttendantRepository;
import com.teknokote.ess.dto.shifts.AffectedPumpAttendantDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@Getter
@Setter
public class AffectedPumpAttendantDaoImpl extends JpaGenericDao<Long, AffectedPumpAttendantDto, AffectedPumpAttendant> implements AffectedPumpAttendantDao {
    @Autowired
    private AffectedPumpAttendantMapper mapper;
    @Autowired
    private AffectedPumpAttendantRepository repository;

    @Override
    public AffectedPumpAttendant beforeCreate(AffectedPumpAttendant affectedPumpAttendant, AffectedPumpAttendantDto dto) {
        if (affectedPumpAttendant.getPumpAttendantId() != null && affectedPumpAttendant.getPumpId() != null) {
            affectedPumpAttendant.setPumpAttendant(getEntityManager().getReference(PumpAttendant.class, dto.getPumpAttendantId()));
        }
        if (affectedPumpAttendant.getPumpId() != null && affectedPumpAttendant.getPumpAttendantId() != null) {
            affectedPumpAttendant.setPump(getEntityManager().getReference(Pump.class, dto.getPumpId()));
        }
        affectedPumpAttendant.setPumpAttendantTeam(getEntityManager().getReference(PumpAttendantTeam.class, dto.getPumpAttendantTeamId()));

        return super.beforeCreate(affectedPumpAttendant, dto);
    }
}
