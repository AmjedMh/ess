package com.teknokote.ess.core.dao.mappers.shifts;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.shifts.ShiftPlanningExecutionDetail;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface ShiftPlanningExecutionDetailMapper extends BidirectionalEntityDtoMapper<Long, ShiftPlanningExecutionDetail, ShiftPlanningExecutionDetailDto> {
    @Override
    @Mapping(target = "shiftPlanningExecutionId", expression = "java(source.getShiftPlanningExecution()==null?null:source.getShiftPlanningExecution().getId())")
    @Mapping(target = "pumpAttendantId", expression = "java(source.getPumpAttendant()==null?null:source.getPumpAttendant().getId())")
    @Mapping(target = "pumpId", expression = "java(source.getPump()==null?null:source.getPump().getId())")
    @Mapping(target = "nozzleId", expression = "java(source.getNozzle()==null?null:source.getNozzle().getId())")
    @Mapping(target = "pumpIdConf", expression = "java(source.getPump()==null?null:source.getPump().getIdConf())")
    @Mapping(target = "nozzleIdConf", expression = "java(source.getNozzle()==null?null:source.getNozzle().getIdConf())")
    @Mapping(target = "nozzleName", expression = "java(source.getNozzle()==null?null:source.getNozzle().getGrade().getName())")
    ShiftPlanningExecutionDetailDto toDto(ShiftPlanningExecutionDetail source);

    @Override
    @Mapping(target = "pumpAttendant", ignore = true)
    @Mapping(target = "nozzle", ignore = true)
    ShiftPlanningExecutionDetail toEntity(ShiftPlanningExecutionDetailDto source);
}
