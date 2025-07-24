package com.teknokote.ess.core.service;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.authentification.model.LoginRequest;
import com.teknokote.ess.authentification.model.LoginResponse;
import com.teknokote.ess.core.dao.CustomerAccountDao;
import com.teknokote.ess.core.dao.UserDao;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Getter
@Setter
@Slf4j
public class LoginService
{
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
   @Autowired
   private UserDao userDao;
   @Autowired
   CustomerAccountDao customerAccountDao;
   private final RestTemplate restTemplate;

   @Autowired
   public LoginService(RestTemplate restTemplate, UserDao userDao, CustomerAccountDao customerAccountDao) {
      this.restTemplate = restTemplate;
      this.userDao = userDao;
      this.customerAccountDao = customerAccountDao;
   }

   public LoginResponse login(LoginRequest loginrequest, boolean doCheckUser) {
      validateLoginRequest(loginrequest, doCheckUser);

      HttpEntity<MultiValueMap<String, String>> httpEntity = createTokenRequest(loginrequest);
      log.info("Trying to log in: {}", loginrequest.getUsername());
      ResponseEntity<LoginResponse> response = restTemplate.postForEntity(tokenEndpoint, httpEntity, LoginResponse.class);

      if (!response.hasBody() || response.getBody() == null) {
         log.warn("{} -> Failed to log in, response body is null", loginrequest.getUsername());
         throw new BadCredentialsException("Login failed: Invalid credentials or server error.");
      }

      userDao.updateLastConnection(loginrequest.getUsername(), LocalDateTime.now());
      log.info("{} -> logged in", loginrequest.getUsername());

      addCustomerAccountId(loginrequest, response.getBody());

      return response.getBody();
   }

   public void validateLoginRequest(LoginRequest loginrequest, boolean doCheckUser) {
      if (doCheckUser) {
         if (Objects.isNull(loginrequest.getUsername())) {
            throw new BadCredentialsException("Username or password mismatch");
         }
         if (userDao.getRepository().findUserByUsernameIgnoreCase(loginrequest.getUsername()).isEmpty()) {
            throw new BadCredentialsException("Username or password mismatch");
         }
      }
   }

   public HttpEntity<MultiValueMap<String, String>> createTokenRequest(LoginRequest loginrequest) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
      map.add("client_id", clientId);
      map.add("client_secret", clientSecret);
      map.add("grant_type", grantType);
      map.add("username", loginrequest.getUsername());
      map.add("password", loginrequest.getPassword());

      return new HttpEntity<>(map, headers);
   }

   public void addCustomerAccountId(LoginRequest loginrequest, LoginResponse response) {
      userDao.getRepository()
              .findUserByUsernameIgnoreCase(loginrequest.getUsername())
              .flatMap(user -> customerAccountDao.getRepository().findAllByMasterUsername(user.getUsername()))
              .ifPresent(customerAccount -> {
                 if (loginrequest.isAdmin() && !customerAccount.isResaleRight()) {
                    log.warn("User {} does not have resale rights and cannot log in to the administration platform", loginrequest.getUsername());
                    throw new ServiceValidationException("Access denied: You do not have the resale rights required to log in to the administration platform.");
                 }
                 response.setCustomerAccountId(customerAccount.getId());
              });
   }
}
