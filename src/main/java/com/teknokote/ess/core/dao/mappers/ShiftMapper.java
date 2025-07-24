package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.shifts.Shift;
import com.teknokote.ess.dto.ShiftDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface ShiftMapper extends BidirectionalEntityDtoMapper<Long, Shift, ShiftDto>
{
}
