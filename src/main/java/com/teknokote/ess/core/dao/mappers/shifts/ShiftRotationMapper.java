package com.teknokote.ess.core.dao.mappers.shifts;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.shifts.ShiftRotation;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface ShiftRotationMapper extends BidirectionalEntityDtoMapper<Long, ShiftRotation, ShiftRotationDto>
{
}
