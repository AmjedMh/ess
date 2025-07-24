package com.teknokote.ess.core.dao.mappers.shifts;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.shifts.ShiftPlanning;
import com.teknokote.ess.dto.shifts.ShiftPlanningDto;
import org.mapstruct.*;

@Mapper(config = MapperConfiguration.class)
public interface ShiftPlanningMapper extends BidirectionalEntityDtoMapper<Long, ShiftPlanning, ShiftPlanningDto>
{
   @Override
   @Mapping(target = "shiftRotation",ignore = true)
   @Mapping(target = "pumpAttendantTeam",ignore = true)
   @Mapping(target = "shift",ignore = true)
   @Mapping(target = "workDayShiftPlanning",ignore = true)
   ShiftPlanning toEntity(ShiftPlanningDto source);

   @Override
   @Mapping(target = "shiftRotation",ignore = true)
   @Mapping(target = "pumpAttendantTeam",ignore = true)
   @Mapping(target = "shift",ignore = true)
   @Mapping(target = "pumpAttendantTeamId",source = "pumpAttendantTeamId",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
   @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
   ShiftPlanning update(ShiftPlanningDto source, @MappingTarget ShiftPlanning target);
}
