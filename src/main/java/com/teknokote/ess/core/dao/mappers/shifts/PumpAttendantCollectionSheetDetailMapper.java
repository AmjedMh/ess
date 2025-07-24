package com.teknokote.ess.core.dao.mappers.shifts;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.shifts.PumpAttendantCollectionSheetDetail;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface PumpAttendantCollectionSheetDetailMapper extends BidirectionalEntityDtoMapper<Long, PumpAttendantCollectionSheetDetail, PumpAttendantCollectionSheetDetailDto>
{
   @Override
   @Mapping(target = "paymentMethod" , ignore = true)
   PumpAttendantCollectionSheetDetail toEntity(PumpAttendantCollectionSheetDetailDto source);
}
