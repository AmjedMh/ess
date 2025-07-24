package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionnelMapper;
import com.teknokote.ess.dto.UserDto;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface UserControllerMapper extends BidirectionnelMapper<UserRepresentation, UserDto>
{

}
