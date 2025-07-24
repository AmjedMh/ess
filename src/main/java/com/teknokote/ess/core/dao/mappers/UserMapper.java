package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.dto.UserDto;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper extends BidirectionalEntityDtoMapper<Long, User, UserDto> {
    @Mapping(target = "enabled", constant = "true")
    UserRepresentation toUserRepresentation(UserDto userDto);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "firstName", target = "firstName") // Add more attributes as needed
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(target = "phone", expression = "java(mapPhoneAttribute(userRepresentation))")
    @Mapping(target = "id", ignore = true)
    UserDto userRepresentationToDto(UserRepresentation userRepresentation);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "firstName", target = "firstName") // Add more attributes as needed
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(target = "phone", expression = "java(mapPhoneAttribute(userRepresentation))")
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserDto enrichDtoFromUserRepresentation(UserRepresentation userRepresentation, @MappingTarget UserDto target);

    @Mapping(source = "customerAccount.name", target = "customerAccountName")
    @Mapping(source = "creatorAccount.name", target = "creatorCustomerAccountName")
    @Mapping(source = "audit.createdDate", target = "createdDate")
    @Override
    UserDto toDto(User source);

    @Override
    User toEntity(UserDto source);

    default String mapPhoneAttribute(UserRepresentation userRepresentation) {
        // Retrieve the phone attribute from UserRepresentation
        Map<String, List<String>> attributes = userRepresentation.getAttributes();
        if (attributes != null && attributes.containsKey("phone")) {
            return userRepresentation.getAttributes().get("phone").get(0);
        } else {
            return null; // or handle accordingly if it's not present
        }
    }

}
