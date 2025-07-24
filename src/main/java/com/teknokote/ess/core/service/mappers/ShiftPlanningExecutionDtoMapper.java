package com.teknokote.ess.core.service.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningExecutionDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class, uses = { ShiftPlanningExecutionDetailDtoMapper.class})
public interface ShiftPlanningExecutionDtoMapper
{
   @Mapping(target = "dynamicShiftPlanningExecutionDetail", source = "shiftPlanningExecutionDetail")
   @Mapping(target = "shiftIndex", source = "shift.index")
   DynamicShiftPlanningExecutionDto toDynamic(ShiftPlanningExecutionDto source);
}
