package com.teknokote.ess.http.logger;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.authentification.model.LoginResponse;
import com.teknokote.ess.core.dao.UserDao;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.dto.UserDto;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;

@Service
public class KeycloakToken {
    @Value("${spring.security.oauth2.client.provider.keycloak.realm}")
    public String realm;
    @Value("${spring.security.oauth2.client.provider.keycloak.serverUrl}")
    private String serverUrl;
    @Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.provider.keycloak.token_endpoint}")
    private String tokenEndpoint;
    @Autowired
    UserDao userDao;

    private final RestTemplate restTemplate = new RestTemplate();
    private Keycloak instance;

    public Keycloak getInstance() {
        if (Objects.isNull(this.instance)) {
            this.instance = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
        }
        return this.instance;
    }
    public Optional<UserRepresentation> getUserIdentity(String username) {
        Keycloak keycloak = this.getInstance();
        return keycloak.realm(realm).users().searchByUsername(username, true).stream().findFirst();
    }
    public Optional<UserRepresentation> resetUserPasswordIdentity(String email) {
        Keycloak keycloak = this.getInstance();
        return keycloak.realm(realm).users().searchByEmail(email, true).stream().findFirst();
    }
    public void passwordReset(String email, UserDto userDto) {
        Keycloak keycloak = this.getInstance();
        Optional<UserRepresentation> user = resetUserPasswordIdentity(email);
        if (user.isPresent()) {
            UserRepresentation userRepresentation = user.get();
            // Initiate "Forgot Password" flow
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResourceInRealm = realmResource.users();
            UserResource userResource = usersResourceInRealm.get(userRepresentation.getId());
            // Set user password
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(userDto.getPassword());
            passwordCred.setTemporary(false);
            userResource.resetPassword(passwordCred);
        } else {
            throw new EntityNotFoundException("User with mail: " + email + " not found");
        }
    }
    public Optional<UserRepresentation> findUserByEmail(String email) {
        return this.getInstance().realm(realm).users().searchByEmail(email, true).stream().findFirst();
    }
    public LoginResponse getImpersonationToken(String accessToken, String targetUserId) {

        // Prepare HTTP request
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        headers.set("Authorization", "Bearer " + accessToken);

        // Prepare request parameters using MultiValueMap
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        params.add("subject_token", accessToken);
        params.add("requested_token_type", "urn:ietf:params:oauth:token-type:access_token");
        params.add("requested_subject", targetUserId);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
        // Execute HTTP request
        ResponseEntity<LoginResponse> response = restTemplate.exchange(
                tokenEndpoint,
                HttpMethod.POST,
                requestEntity,
                LoginResponse.class
        );
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new ServiceValidationException("Failed to get impersonation token, status: " + response.getStatusCode());
        }
    }
    public LoginResponse impersonateUser(User user, UserDto userDto, String accessToken) {
        if (userDto != null) {
            Optional<UserRepresentation> targetUser = getUserIdentity(userDto.getUsername().toLowerCase());
            if (targetUser.isPresent()) {
                LoginResponse response = getImpersonationToken(accessToken, targetUser.get().getId());
                response.setOriginalUserId(user.getId());
                response.setImpersonationMode(true);
                response.setCustomerAccountId(userDto.getCustomerAccountId());
                return response;
            }
            throw new ServiceValidationException("Target user identity not found");
        }
        throw new ServiceValidationException("User not found");
    }
    public LoginResponse exitImpersonation(String accessToken, Long targetUserId) {
        Optional<UserDto> userDto = userDao.findById(targetUserId);
        if (userDto.isPresent()) {
            Optional<UserRepresentation> targetUser = getUserIdentity(userDto.get().getUsername().toLowerCase());

            if (targetUser.isPresent()) {
                LoginResponse response = getImpersonationToken(accessToken, targetUser.get().getId());
                response.setOriginalUserId(null);
                response.setImpersonationMode(false);
                response.setCustomerAccountId(userDto.get().getCustomerAccountId());
                return response;
            }
            throw new ServiceValidationException("Target user identity not found");
        }
        throw new ServiceValidationException("User not found");
    }
}
