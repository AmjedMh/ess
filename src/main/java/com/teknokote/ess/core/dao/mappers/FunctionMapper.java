package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.organization.Function;
import com.teknokote.ess.dto.FunctionDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface FunctionMapper extends BidirectionalEntityDtoMapper<Long, Function, FunctionDto>
{
}
