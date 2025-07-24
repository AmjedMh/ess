package com.teknokote.ess.core.dao.mappers.shifts;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.shifts.ShiftPlanningExecution;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class, uses = { ShiftPlanningExecutionDetailMapper.class})
public interface ShiftPlanningExecutionMapper extends BidirectionalEntityDtoMapper<Long, ShiftPlanningExecution, ShiftPlanningExecutionDto>
{
}
