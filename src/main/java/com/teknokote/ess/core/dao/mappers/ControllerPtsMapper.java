package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.dto.ControllerPtsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfiguration.class, uses = {FirmwareInformationMapper.class})
public interface ControllerPtsMapper extends BidirectionalEntityDtoMapper<Long, ControllerPts, ControllerPtsDto> {
    @Override
    @Mapping(target = "currentFirmwareInformation", ignore = true)
    ControllerPts toEntity(ControllerPtsDto source);

    @Override
    @Mapping(target = "currentFirmwareInformation", ignore = true)
    @Mapping(target = "userController")
    ControllerPts update(ControllerPtsDto source, @MappingTarget ControllerPts target);
}
