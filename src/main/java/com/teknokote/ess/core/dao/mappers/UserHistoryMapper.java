package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.UserHistory;
import com.teknokote.ess.dto.UserHistoryDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface UserHistoryMapper extends BidirectionalEntityDtoMapper<Long, UserHistory, UserHistoryDto>
{
}
