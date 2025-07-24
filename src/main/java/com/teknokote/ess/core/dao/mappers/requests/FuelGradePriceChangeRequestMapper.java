package com.teknokote.ess.core.dao.mappers.requests;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.requests.FuelGradePriceChangeRequest;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.requests.FuelGradePriceChangeRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface FuelGradePriceChangeRequestMapper extends BidirectionalEntityDtoMapper<Long, FuelGradePriceChangeRequest, FuelGradePriceChangeRequestDto>
{

   @Mapping(target = "idConf", source = "fuelGrade.idConf")
   @Mapping(target = "id", source = "fuelGrade.id")
   @Mapping(target = "expansionCoefficient", source = "fuelGrade.expansionCoefficient")
   @Mapping(target = "name", source = "fuelGrade.name")
   @Mapping(target = "price", source = "newPrice")
   FuelGradeConfigDto toFuelGradeConfig(FuelGradePriceChangeRequestDto fuelGradePriceChangeRequest);
   @Override
   @Mapping(target = "createdDate", source = "audit.createdDate")
   FuelGradePriceChangeRequestDto toDto(FuelGradePriceChangeRequest source);
}
