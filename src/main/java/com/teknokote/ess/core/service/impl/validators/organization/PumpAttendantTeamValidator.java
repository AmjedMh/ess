package com.teknokote.ess.core.service.impl.validators.organization;

import com.teknokote.ess.core.dao.organization.PumpAttendantTeamDao;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class PumpAttendantTeamValidator implements ESSValidator<PumpAttendantTeamDto>
{
    @Autowired
    PumpAttendantTeamDao teamDao;
    @Override
    public ESSValidationResult validateOnCreate(PumpAttendantTeamDto pumpAttendantTeamDto)
    {
        ESSValidationResult validationResult= ESSValidator.super.validateOnCreate(pumpAttendantTeamDto);
        if(Objects.isNull(pumpAttendantTeamDto.getShiftRotationId())){
            validationResult.foundConsistencyError("Shift Rotation not defined !!!");
        }
        else
        {
            final Optional<PumpAttendantTeamDto> pumpAttendantTeam = teamDao.findByRotationAndName(pumpAttendantTeamDto.getStationId(),pumpAttendantTeamDto.getShiftRotationId(),pumpAttendantTeamDto.getName());
            pumpAttendantTeam.ifPresent(controllerPtsDto -> validationResult.foundConsistencyError("existing tem with the same name "));
        }
        return validationResult;
    }

}
