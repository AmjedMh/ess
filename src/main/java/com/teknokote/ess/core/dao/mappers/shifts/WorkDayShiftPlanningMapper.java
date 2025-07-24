package com.teknokote.ess.core.dao.mappers.shifts;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.shifts.WorkDayShiftPlanning;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface WorkDayShiftPlanningMapper extends BidirectionalEntityDtoMapper<Long, WorkDayShiftPlanning, WorkDayShiftPlanningDto>
{
}
