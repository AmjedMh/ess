package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.dto.StationDto;
import org.mapstruct.*;

@Mapper(config = MapperConfiguration.class, uses = {ControllerPtsMapper.class, CountryMapper.class})
public interface StationMapper extends BidirectionalEntityDtoMapper<Long, Station, StationDto> {
    @Override
    @Mapping(target = "country", ignore = true)
    Station toEntity(StationDto source);

    @Mapping(source = "customerAccount.name", target = "customerAccountName")
    @Mapping(source = "creatorAccount.name", target = "creatorCustomerAccountName")
    @Mapping(source = "controllerPts.id", target = "controllerPtsId")
    @Mapping(source = "audit.createdDate", target = "createdDate")
    @Override
    StationDto toDto(Station source);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "controllerPts")
    Station update(StationDto source, @MappingTarget Station target);

    @AfterMapping
    default void calledWithSourceAndTarget(StationDto source, @MappingTarget Station target) {
        target.getControllerPts().attachStation(target);
    }
}
