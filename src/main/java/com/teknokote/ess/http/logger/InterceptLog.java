package com.teknokote.ess.http.logger;

import com.teknokote.ess.core.model.EnumActivityType;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.UserHistoryService;
import com.teknokote.ess.core.service.impl.UserService;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.dto.UserHistoryDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class InterceptLog implements HandlerInterceptor {

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private UserHistoryService userHistoryService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private UserService userService;
    private static final String IMPERSONATOR = "impersonator";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = null;
        boolean impersonationMode = false;
        String impersonatorUser = null;

        if (Objects.nonNull(request.getUserPrincipal())) {
            userId = getUserIdFromAuthentication();
            Map<String, Object> claims = getJwtClaimsFromRequest(request);
            if (claims != null) {
                impersonationMode = checkForImpersonation(claims);
                impersonatorUser = getImpersonatorUsername(claims);
            }
        }

        createUserHistory(request, handler, userId, impersonationMode, impersonatorUser);

        if (isLoggableRequest(request)) {
            loggingService.logRequest(request, null);
        }

        return true;
    }

    private Long getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    private Map<String, Object> getJwtClaimsFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getClaims();
        }
        return Collections.emptyMap();
    }

    private boolean checkForImpersonation(Map<String, Object> claims) {
        return claims.containsKey(IMPERSONATOR);
    }

    private String getImpersonatorUsername(Map<String, Object> claims) {
        if (claims.containsKey(IMPERSONATOR)) {
            Map<String, Object> impersonator = (Map<String, Object>) claims.get(IMPERSONATOR);
            String impersonatorUserName = (String) impersonator.get("username");
            if (impersonatorUserName != null) {
                Optional<UserDto> userDto = userService.findByUsername(impersonatorUserName);
                if (userDto.isPresent()) {
                    return userDto.get().getUsername();
                }
            }
        }
        return null;
    }

    private void createUserHistory(HttpServletRequest request, Object handler, Long userId, boolean impersonationMode, String impersonatorUser) {
        UserHistoryDto userHistoryDto = UserHistoryDto.builder()
                .userId(userId)
                .ipAddress(request.getRemoteAddr())
                .requestURI(request.getRequestURI())
                .activityDate(LocalDateTime.now())
                .activityType(EnumActivityType.of(request.getMethod()))
                .requestHandler(handler.toString())
                .impersonationMode(impersonationMode)
                .impersonatorUserName(impersonatorUser)
                .build();

        userHistoryService.create(userHistoryDto);
    }

    private boolean isLoggableRequest(HttpServletRequest request) {
        return request.getMethod().equals(HttpMethod.GET.name())
                || request.getMethod().equals(HttpMethod.DELETE.name())
                || request.getMethod().equals(HttpMethod.PUT.name());
    }
}
