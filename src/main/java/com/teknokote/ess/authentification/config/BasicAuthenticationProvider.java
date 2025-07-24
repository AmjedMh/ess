package com.teknokote.ess.authentification.config;


import com.teknokote.ess.authentification.model.LoginRequest;
import com.teknokote.ess.authentification.model.LoginResponse;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.LoginService;
import com.teknokote.ess.core.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class BasicAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private UserService userService;
    @Autowired
    private LoginService loginService;
    private static final String USERNAME_OR_PASSWORD_MISMATCH = "username or password mismatch";

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        if (Objects.isNull(username)) throw new BadCredentialsException(USERNAME_OR_PASSWORD_MISMATCH);
        final Optional<User> user = userService.getDao().getRepository().findUserByUsernameIgnoreCase(username.toLowerCase());
        if (user.isEmpty()) throw new BadCredentialsException(USERNAME_OR_PASSWORD_MISMATCH);
        final LoginResponse loginResponse = loginService.login(LoginRequest.init(username, password,false), false);
        if (Objects.isNull(loginResponse.getAccessToken()))
            throw new BadCredentialsException(USERNAME_OR_PASSWORD_MISMATCH);
        return new UsernamePasswordAuthenticationToken(user.get(), null, getUserRoles(Collections.singleton("admin")));
    }

    private List<SimpleGrantedAuthority> getUserRoles(Set<String> userRoles) {
        return userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.replaceAll("\\s+", "")))
                .toList();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}

