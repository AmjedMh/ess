package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.organization.CustomerAccount;
import com.teknokote.ess.dto.CustomerAccountDto;
import org.mapstruct.*;

@Mapper(config = MapperConfiguration.class, uses = {StationMapper.class, UserMapper.class})
public interface CustomerAccountMapper extends BidirectionalEntityDtoMapper<Long, CustomerAccount, CustomerAccountDto> {
    @Override
    @Mapping(target = "stations", ignore = true)
    @Mapping(target = "attachedUsers", ignore = true)
    CustomerAccount toEntity(CustomerAccountDto source);

    @Mapping(source = "parent.name", target = "parentName")
    @Mapping(source = "creatorAccount.name", target = "creatorCustomerAccountName")
    @Mapping(source = "audit.createdDate", target = "createdDate")
    @Override
    CustomerAccountDto toDto(CustomerAccount source);

    @Override
    @Mapping(target = "stations", ignore = true)
    @Mapping(target = "attachedUsers", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CustomerAccount update(CustomerAccountDto source, @MappingTarget CustomerAccount target);

    @AfterMapping
    default void calledWithSourceAndTarget(CustomerAccountDto source, @MappingTarget CustomerAccount target) {
        if (target.getMasterUser() != null) {
            target.getMasterUser().setCustomerAccount(target);
        }
        if (target.getStations() != null) {
            target.getStations().forEach(station -> station.setCustomerAccount(target));
        }
    }
}

