package com.teknokote.ess.authentification.config;

import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.impl.CustomerAccountService;
import com.teknokote.ess.core.service.impl.UserService;
import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.ess.dto.CustomerAccountDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UsernamePasswordAuthenticationTokenConverter implements Converter<JwtAuthenticationToken, UsernamePasswordAuthenticationToken> {
    @Autowired
    private UserService userService;
    @Autowired
    private CustomerAccountService customerAccountService;

    @Override
    public UsernamePasswordAuthenticationToken convert(JwtAuthenticationToken source) {
        final Optional<User> user = userService.getDao().getRepository().findUserByUsernameIgnoreCase(source.getName());
        User connectedUser = user.orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<Long> children = customerAccountService.findChildCustomerAccounts(connectedUser).stream().map(CustomerAccountDto::getId).toList();
        children.forEach(connectedUser::addChildCustomerAccount);
        return new UsernamePasswordAuthenticationToken(connectedUser, source.getToken(), source.getAuthorities());
    }
}
