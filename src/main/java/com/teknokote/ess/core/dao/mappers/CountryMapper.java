package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.Country;
import com.teknokote.ess.dto.CountryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfiguration.class, uses = { CurrencyMapper.class})
public interface CountryMapper extends BidirectionalEntityDtoMapper<Long,Country, CountryDto>
{
   @Override
   @Mapping(target = "currency", ignore = true)
   Country toEntity(CountryDto source);

   @Override
   @Mapping(target = "currency", ignore = true)
   Country update(CountryDto source, @MappingTarget Country target);
}
