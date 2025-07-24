package com.teknokote.ess.core.dao.mappers.shifts;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.shifts.AffectedPumpAttendant;
import com.teknokote.ess.dto.shifts.AffectedPumpAttendantDto;
import org.mapstruct.*;


@Mapper(config = MapperConfiguration.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AffectedPumpAttendantMapper extends BidirectionalEntityDtoMapper<Long, AffectedPumpAttendant, AffectedPumpAttendantDto> {

    @Override
    @Mapping(target = "pumpAttendant", ignore = true)
    AffectedPumpAttendant toEntity(AffectedPumpAttendantDto source);

}
