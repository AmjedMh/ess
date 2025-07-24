package com.teknokote.ess.http.logger;

import com.teknokote.ess.core.model.EnumActivityType;
import com.teknokote.ess.core.service.UserHistoryService;
import com.teknokote.ess.core.service.impl.UserService;
import com.teknokote.ess.dto.UserHistoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Component
public class EntityActionEventListener {

    @Autowired
    private UserHistoryService userHistoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtDecoder jwtDecoder;

    @EventListener
    public void handleEntityActionEvent(EntityActionEvent event) {
        UserHistoryDto userHistory = createUserHistory(event);
        userHistoryService.create(userHistory);
    }

    private UserHistoryDto createUserHistory(EntityActionEvent event) {
        Long userId = null;
        String userName = null;
        boolean impersonationMode = false;
        String impersonatorUser = null;

        if (isUserAuthenticated(event)) {
            Jwt jwt = extractJwtFromRequest(event);
            if (jwt != null) {
                impersonationMode = isImpersonation(jwt);
                if (impersonationMode) {
                    impersonatorUser = getImpersonatorUserName(jwt);
                }
            }
            userId = event.getUser() != null ? event.getUser().getId() : null;
            userName = event.getUser() != null ? event.getUser().getUsername() : "SYSTEM";
        } else {
            userId = event.getUser() != null ? event.getUser().getId() : null;
            userName = "SYSTEM";
        }

        return buildUserHistoryDto(event, userId, userName, impersonationMode, impersonatorUser);
    }

    private boolean isUserAuthenticated(EntityActionEvent event) {
        return Objects.nonNull(event.getServletRequest()) &&
                Objects.nonNull(event.getServletRequest().getUserPrincipal());
    }

    private Jwt extractJwtFromRequest(EntityActionEvent event) {
        String authorizationHeader = event.getServletRequest().getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return jwtDecoder.decode(token);
        }
        return null;
    }

    private boolean isImpersonation(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        return claims.containsKey("impersonator");
    }

    private String getImpersonatorUserName(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        Map<String, Object> impersonator = (Map<String, Object>) claims.get("impersonator");
        return (String) impersonator.get("username");
    }

    private UserHistoryDto buildUserHistoryDto(EntityActionEvent event, Long userId, String userName,
                                               boolean impersonationMode, String impersonatorUser) {
        return UserHistoryDto.builder()
                .userId(userId)
                .userName(userName)
                .activityDate(LocalDateTime.now())
                .ipAddress(event.getServletRequest() != null ? event.getServletRequest().getRemoteAddr() : "N/A")
                .requestURI(event.getServletRequest() != null ? event.getServletRequest().getRequestURI() : "N/A")
                .activityType(event.getServletRequest() != null ? EnumActivityType.of(event.getServletRequest().getMethod()) : null)
                .impersonationMode(impersonationMode)
                .impersonatorUserName(impersonatorUser)
                .action(event.getAction())
                .build();
    }
}

