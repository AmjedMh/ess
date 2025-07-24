package com.teknokote.ess.core.dao.mappers.shifts;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.shifts.PumpAttendantCollectionSheet;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class, uses = { PumpAttendantCollectionSheetDetailMapper.class})
public interface PumpAttendantCollectionSheetMapper extends BidirectionalEntityDtoMapper<Long, PumpAttendantCollectionSheet, PumpAttendantCollectionSheetDto>
{
}
