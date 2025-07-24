package com.teknokote.ess.core.service.Entities;

import com.teknokote.ess.authentification.config.BasicAuthenticationProvider;
import com.teknokote.ess.authentification.model.LoginRequest;
import com.teknokote.ess.authentification.model.LoginResponse;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.repository.UserRepository;
import com.teknokote.ess.core.service.LoginService;
import com.teknokote.ess.core.service.impl.UserService;
import com.teknokote.ess.core.dao.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BasicAuthenticationProviderTest {

    @InjectMocks
    private BasicAuthenticationProvider authenticationProvider;

    @Mock
    private UserService userService;

    @Mock
    private LoginService loginService;

    @Mock
    private UserDao userDao;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock the UserService to return our mocked UserDao
        when(userService.getDao()).thenReturn(userDao);
        when(userDao.getRepository()).thenReturn(userRepository);
    }

    @Test
    void testAuthenticateValidCredentials() {
        // Given
        String username = "user";
        String password = "password";
        User user = new User();
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken("valid_token");

        when(userRepository.findUserByUsernameIgnoreCase(username.toLowerCase()))
                .thenReturn(Optional.of(user));
        when(loginService.login(any(LoginRequest.class), eq(false))).thenReturn(loginResponse);

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);

        // When
        Authentication result = authenticationProvider.authenticate(authentication);

        // Then
        assertNotNull(result);
        assertEquals(user, result.getPrincipal());
        assertEquals("valid_token", loginResponse.getAccessToken());
    }

    @Test
    void testAuthenticateInvalidUsername() {
        // Given
        String username = "invalidUser";
        String password = "password";

        when(userRepository.findUserByUsernameIgnoreCase(username.toLowerCase()))
                .thenReturn(Optional.empty());

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authenticationProvider.authenticate(authentication);
        });
        assertEquals("username or password mismatch", exception.getMessage());
    }

    @Test
    void testAuthenticateInvalidPassword() {
        // Given
        String username = "user";
        String password = "wrongPassword";
        User user = new User();
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(null);

        when(userRepository.findUserByUsernameIgnoreCase(username.toLowerCase()))
                .thenReturn(Optional.of(user));
        when(loginService.login(any(LoginRequest.class), eq(false))).thenReturn(loginResponse);

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authenticationProvider.authenticate(authentication);
        });
        assertEquals("username or password mismatch", exception.getMessage());
    }

    @Test
    void testSupports() {
        assertTrue(authenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
        assertFalse(authenticationProvider.supports(Object.class));
    }
}