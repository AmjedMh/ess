package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.organization.UserScope;
import com.teknokote.ess.dto.UserScopeDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface UserScopeMapper extends BidirectionalEntityDtoMapper<Long, UserScope, UserScopeDto> {

}
