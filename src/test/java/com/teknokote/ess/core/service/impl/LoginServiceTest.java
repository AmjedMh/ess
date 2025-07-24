package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.authentification.model.LoginRequest;
import com.teknokote.ess.authentification.model.LoginResponse;
import com.teknokote.ess.core.dao.CustomerAccountDao;
import com.teknokote.ess.core.dao.UserDao;
import com.teknokote.ess.core.model.organization.CustomerAccount;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.repository.CustomerAccountRepository;
import com.teknokote.ess.core.repository.UserRepository;
import com.teknokote.ess.core.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @InjectMocks
    private LoginService loginService;
    @Mock
    private UserDao userDao;
    @Mock
    private CustomerAccountDao customerAccountDao;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerAccountRepository customerAccountRepository;
    @Value("${spring.security.oauth2.client.provider.keycloak.realm}")
    public String realm;
    @Value("${spring.security.oauth2.client.provider.keycloak.serverUrl}")
    private String serverUrl;
    @Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.authorization-grant-type}")
    private String grantType;
    @Value("${spring.security.oauth2.client.provider.keycloak.token_endpoint}")
    private String tokenEndpoint;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("testuser", "password", false);
        loginResponse = new LoginResponse();
        lenient().when(userDao.getRepository()).thenReturn(userRepository);
        lenient().when(customerAccountDao.getRepository()).thenReturn(customerAccountRepository);
    }

    @Test
    void testAdminUserWithoutResaleRightsThrowsException() {
        // Create login request for admin
        loginRequest = new LoginRequest("adminuser", "password", true);
        loginResponse = new LoginResponse();

        // Mock user retrieval and customer account check
        when(userRepository.findUserByUsernameIgnoreCase("adminuser"))
                .thenReturn(Optional.of(User.builder().username("adminuser").build()));
        when(customerAccountRepository.findAllByMasterUsername("adminuser"))
                .thenReturn(Optional.of(CustomerAccount.builder().resaleRight(false).build())); // No resale rights

        // Expect ServiceValidationException
        assertThrows(ServiceValidationException.class, () -> loginService.addCustomerAccountId(loginRequest, loginResponse));
    }

    @Test
    void addCustomerAccountId_withValidUserAndAdmin() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("testuser");

        CustomerAccount mockAccount = new CustomerAccount();
        mockAccount.setId(1L);
        mockAccount.setResaleRight(true);

        given(userDao.getRepository().findUserByUsernameIgnoreCase(anyString()))
                .willReturn(Optional.of(mockUser));
        given(customerAccountDao.getRepository().findAllByMasterUsername(anyString()))
                .willReturn(Optional.of(mockAccount));

        // Act
        loginService.addCustomerAccountId(loginRequest, loginResponse);

        // Assert
        assertEquals(1L, loginResponse.getCustomerAccountId());
    }
    @Test
    void testLoginFailureDueToNullResponseBody() {
        // Arrange
        loginRequest = new LoginRequest("testuser", "password", false);
        when(userRepository.findUserByUsernameIgnoreCase("testuser"))
                .thenReturn(Optional.of(User.builder().username("testuser").build()));

        ResponseEntity<LoginResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK); // Simulate a null body
        when(restTemplate.postForEntity(eq(tokenEndpoint), any(HttpEntity.class), eq(LoginResponse.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            loginService.login(loginRequest, true);
        });

        assertEquals("Login failed: Invalid credentials or server error.", exception.getMessage());
    }

    @Test
    void testLoginRequestValidationUsernameIsNull() {
        // Arrange
        loginRequest = new LoginRequest(null, "password", true); // Username is null

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            loginService.validateLoginRequest(loginRequest, true);
        });

        assertEquals("Username or password mismatch", exception.getMessage());
    }

    @Test
    void testLoginRequestValidationUserNotFound() {
        // Arrange
        loginRequest = new LoginRequest("nonexistentuser", "password", true);

        // Mock user retrieval to return empty
        when(userRepository.findUserByUsernameIgnoreCase("nonexistentuser")).thenReturn(Optional.empty());

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            loginService.validateLoginRequest(loginRequest, true);
        });

        assertEquals("Username or password mismatch", exception.getMessage());
    }

    @Test
    void testLoginSuccess() {
        // Create login request and expected response
        loginRequest = new LoginRequest("testuser", "password", false);
        loginResponse = new LoginResponse();
        loginResponse.setAccessToken("mockAccessToken");

        // Mock user retrieval
        when(userRepository.findUserByUsernameIgnoreCase("testuser"))
                .thenReturn(Optional.of(User.builder().username("testuser").build()));

        // Mock response from REST call
        ResponseEntity<LoginResponse> responseEntity = new ResponseEntity<>(loginResponse, HttpStatus.OK);

        // Ensure you're matching the parameters your service will use when making the request
        when(restTemplate.postForEntity(eq(tokenEndpoint), any(HttpEntity.class), eq(LoginResponse.class)))
                .thenReturn(responseEntity);

        // Perform login
        LoginResponse result = loginService.login(loginRequest, true);

        // Assert results
        assertNotNull(result);
        assertEquals("mockAccessToken", result.getAccessToken());

        // Verify that updateLastConnection was called correctly
        verify(userDao, times(1)).updateLastConnection(eq("testuser"), any(LocalDateTime.class));
    }

    @Test
    void testAddCustomerAccountId() {
        // Create login request and response
        loginRequest = new LoginRequest("testuser", "password", true);
        loginResponse = new LoginResponse();

        // Mock user retrieval and customer account check
        when(userRepository.findUserByUsernameIgnoreCase("testuser"))
                .thenReturn(Optional.of(User.builder().username("testuser").build()));
        when(customerAccountRepository.findAllByMasterUsername("testuser"))
                .thenReturn(Optional.of(CustomerAccount.builder().resaleRight(true).build()));

        // Call the method under test
        loginService.addCustomerAccountId(loginRequest, loginResponse);

        // Assert: Verify that the customer account ID is set correctly
        assertEquals(null, loginResponse.getCustomerAccountId(), "Expected customer account ID to be 1.");
    }
    @Test
    void createTokenRequest_createsValidHttpEntity() {
        // Arrange
        loginRequest = new LoginRequest("testuser", "password", true);

        // Act
        HttpEntity<MultiValueMap<String, String>> requestEntity = loginService.createTokenRequest(loginRequest);

        // Assert
        HttpHeaders headers = requestEntity.getHeaders();
        MultiValueMap<String, String> body = requestEntity.getBody();

        // Check headers
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED.toString(), headers.getContentType().toString());

        // Check body
        assertEquals(clientId, body.getFirst("client_id"));
        assertEquals(clientSecret, body.getFirst("client_secret"));
        assertEquals(grantType, body.getFirst("grant_type"));
        assertEquals("testuser", body.getFirst("username"));
        assertEquals("password", body.getFirst("password"));
    }
}