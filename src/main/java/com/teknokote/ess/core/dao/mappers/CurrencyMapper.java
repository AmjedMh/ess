package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.Currency;
import com.teknokote.ess.dto.CurrencyDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface CurrencyMapper extends BidirectionalEntityDtoMapper<Long,Currency, CurrencyDto>
{
}
