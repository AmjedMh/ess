package com.teknokote.ess.core.dao.mappers.shifts;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.shifts.WorkDayShiftPlanningExecution;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningExecutionDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface WorkDayShiftPlanningExecutionMapper extends BidirectionalEntityDtoMapper<Long, WorkDayShiftPlanningExecution, WorkDayShiftPlanningExecutionDto>
{
}
