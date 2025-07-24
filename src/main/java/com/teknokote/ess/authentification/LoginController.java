package com.teknokote.ess.authentification;

import com.teknokote.ess.authentification.model.LoginRequest;
import com.teknokote.ess.authentification.model.LoginResponse;
import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.LoginService;
import com.teknokote.ess.core.service.impl.UserService;
import com.teknokote.ess.http.logger.KeycloakToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
@RestController
@CrossOrigin("*")
@Slf4j
public class LoginController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserService userService;
    @Autowired
    private KeycloakToken keycloakToken;
    @PostMapping(EndPoints.LOGIN)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginrequest) {
        return new ResponseEntity<>(loginService.login(loginrequest, true), HttpStatus.OK);
    }
    @PostMapping(EndPoints.IMPERSONATE)
    public ResponseEntity<LoginResponse> impersonateUser(@PathVariable Long targetUserId, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String bearerToken = request.getHeader("Authorization");
        LoginResponse tokenResponse = userService.impersonateUser((User)authentication.getPrincipal(),targetUserId,bearerToken.substring(7),request);
            return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping(EndPoints.EXIT_IMPERSONATION)
    public ResponseEntity<LoginResponse> exitImpersonation(@PathVariable Long targetUserId) {
        String adminToken=keycloakToken.getInstance().tokenManager().getAccessTokenString();
        LoginResponse tokenResponse = keycloakToken.exitImpersonation(adminToken,targetUserId);
        return ResponseEntity.ok(tokenResponse);
    }
}
