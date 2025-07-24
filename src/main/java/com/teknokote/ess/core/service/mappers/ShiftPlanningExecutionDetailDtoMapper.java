package com.teknokote.ess.core.service.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningExecutionDetailDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface ShiftPlanningExecutionDetailDtoMapper
{
   @Mapping(target = "pumpAttendantName", source = "pumpAttendant.firstName")
   @Mapping(target = "nozzleName", source = "nozzle.grade.name")
   @Mapping(target = "pumpIdConf", source = "nozzle.pump.idConf")
   @Mapping(target = "nozzleIdConf", source = "nozzle.idConf")
   DynamicShiftPlanningExecutionDetailDto toDynamic(ShiftPlanningExecutionDetailDto source);
}
