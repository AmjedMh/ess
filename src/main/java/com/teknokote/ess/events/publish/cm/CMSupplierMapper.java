package com.teknokote.ess.events.publish.cm;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.organization.CustomerAccount;
import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfiguration.class)
public interface CMSupplierMapper extends BidirectionalEntityDtoMapper<Long, CustomerAccount, CMSupplierDto> {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "identifier", target = "identifier")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "masterUser.phone", target = "phone")
    //@Mapping(source = "address", target = "address")
    @Mapping(source = "masterUser.email", target = "email")
    @Mapping(source = "id", target = "reference")
    @Mapping(source = "stations", target = "salePoints")
    @Mapping(source = "attachedUsers", target = "users")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "id", ignore = true)
    CMSupplierDto customerAccountToSupplier(CustomerAccountDto source);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "customerAccountId", target = "reference")
    @Mapping(source = "actif", target = "status")
    CMSalePointDto mapStationToSalesPoint(StationDto stationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "customerAccountId", target = "reference")
    CMUser mapUserToCMUser(UserDto userDto);

    @Mapping(target = "id", ignore = true)
    CMProduct mapFuelGradeToCMProduct(FuelGradeConfigDto fuelGrade);

    @Named("preserveId")
    default Long preserveId(Long id) {
        return id;
    }
}
