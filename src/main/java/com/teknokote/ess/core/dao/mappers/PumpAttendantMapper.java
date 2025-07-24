package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.dto.PumpAttendantDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface PumpAttendantMapper extends BidirectionalEntityDtoMapper<Long, PumpAttendant, PumpAttendantDto>
{
}
