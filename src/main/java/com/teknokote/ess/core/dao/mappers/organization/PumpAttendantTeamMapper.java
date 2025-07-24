package com.teknokote.ess.core.dao.mappers.organization;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.dao.mappers.shifts.AffectedPumpAttendantMapper;
import com.teknokote.ess.core.model.organization.PumpAttendantTeam;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import org.mapstruct.Mapper;


@Mapper(config = MapperConfiguration.class, uses = {AffectedPumpAttendantMapper.class})
public interface PumpAttendantTeamMapper extends BidirectionalEntityDtoMapper<Long, PumpAttendantTeam, PumpAttendantTeamDto> {

}
