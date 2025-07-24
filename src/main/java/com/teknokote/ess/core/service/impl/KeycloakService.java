package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.core.dao.UserDao;
import com.teknokote.ess.core.dao.mappers.UserMapper;
import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.core.service.UserScopeService;
import com.teknokote.ess.dto.FunctionDto;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.dto.UserScopeDto;
import com.teknokote.ess.exceptions.KeycloakUserCreationException;
import com.teknokote.ess.http.logger.KeycloakToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakService {
    @Value("${spring.security.oauth2.client.provider.keycloak.realm}")
    public String realm;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private KeycloakToken keycloakToken;
    @Autowired
    UserDao userDao;
    @Autowired
    private UserScopeService userScopeService;
    private static final String CUSTOMER_ACCOUNT_ID = "customerAccountId";

    public UserRepresentation createUser(@RequestBody UserDto createUserRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUsername = authentication.getName();
        // Create the user in Keycloak
        Keycloak keycloak = keycloakToken.getInstance();
        UserRepresentation user = userMapper.toUserRepresentation(createUserRequest);
        user.setFirstName(createUserRequest.getFirstName());
        user.setLastName(createUserRequest.getLastName());
        user.setUsername(createUserRequest.getUsername());
        user.setEmail(createUserRequest.getEmail());
        user.setRealmRoles(Collections.emptyList());
        user.setEnabled(true);

        // Set optional attributes
        if (createUserRequest.getPhone() != null) {
            user.singleAttribute("phone", createUserRequest.getPhone());
        }
        if (createUserRequest.getCustomerAccountId() != null) {
            user.singleAttribute(CUSTOMER_ACCOUNT_ID, String.valueOf(createUserRequest.getCustomerAccountId()));
        }

        // Handle function scopes if no CustomerAccountId is provided
        if (createUserRequest.getCustomerAccountId() == null) {
            userDao.findAllByUsername(loggedInUsername).ifPresent(loggedUserDto -> {
                List<UserScopeDto> userScopeDtos = userScopeService.findByFunctionalScopeAndUser(EnumFunctionalScope.GLOBAL, loggedUserDto.getId());
                List<FunctionDto> listFunctions = userScopeDtos.stream()
                        .map(UserScopeDto::getScopeFunctions)
                        .flatMap(Collection::stream)
                        .toList();

                if (!listFunctions.isEmpty()) {
                    String functionScopeNames = listFunctions.stream()
                            .map(functionScope -> functionScope.getCode().name())
                            .collect(Collectors.joining(","));
                    user.singleAttribute("functionScopes", functionScopeNames);
                }
            });
        }

        // Set user password
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(createUserRequest.getPassword());
        user.setCredentials(Collections.singletonList(passwordCred));
        // Set user role
        RealmResource realmResource = keycloak.realm(realm);

        try (Response response = realmResource.users().create(user)) {
            if (response.getStatus() != 201) {
                throw new KeycloakUserCreationException(response.getStatusInfo().getReasonPhrase());
            }
            String locationPath = response.getLocation().getPath();
            String userId = locationPath.substring(locationPath.lastIndexOf('/') + 1);

            // Assign the "impersonation" role to the user
            assignImpersonationRole(keycloak, realmResource, userId);

            // Assign or create the specified role
            if (StringUtils.hasText(createUserRequest.getRole())) {
                assignOrCreateUserRole(realmResource, userId, createUserRequest.getRole());
            }
        } catch (Exception exception) {
            throw new KeycloakUserCreationException(exception);
        }

        return user;
    }
    void assignImpersonationRole(Keycloak keycloak, RealmResource realmResource, String userId) {
        List<ClientRepresentation> clients = keycloak.realm(realm).clients().findByClientId("realm-management");
        if (clients.isEmpty()) {
            throw new ServiceValidationException("Realm-management client not found.");
        }
        String realmManagementClientId = clients.get(0).getId();

        RolesResource rolesResource = keycloak.realm(realm).clients().get(realmManagementClientId).roles();
        RoleRepresentation impersonationRole = rolesResource.list().stream()
                .filter(role -> "impersonation".equals(role.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Impersonation role not found in realm-management client"));

        realmResource.users().get(userId).roles().clientLevel(realmManagementClientId)
                .add(Collections.singletonList(impersonationRole));
    }
    void assignOrCreateUserRole(RealmResource realmResource, String userId, String roleName) {
        UserResource userResource = realmResource.users().get(userId);

        // Remove existing roles
        List<RoleRepresentation> defaultRoles = userResource.roles().realmLevel().listEffective();
        userResource.roles().realmLevel().remove(defaultRoles);

        RolesResource realmRolesResource = realmResource.roles();
        Optional<RoleRepresentation> specifiedRole = realmRolesResource.list().stream()
                .filter(role -> role.getName().equals(roleName))
                .findFirst();

        // Assign existing role or create new one
        if (specifiedRole.isPresent()) {
            userResource.roles().realmLevel().add(Collections.singletonList(specifiedRole.get()));
        } else {
            RoleRepresentation newRole = new RoleRepresentation();
            newRole.setName(roleName);
            realmRolesResource.create(newRole);
            RoleRepresentation createdRole = realmRolesResource.get(roleName).toRepresentation();
            userResource.roles().realmLevel().add(Collections.singletonList(createdRole));
        }
    }
    public Optional<UserRepresentation> getUserIdentity(String username) {
        Keycloak keycloak = keycloakToken.getInstance();
        return keycloak.realm(realm).users().searchByUsername(username, true).stream().findFirst();
    }
    public Optional<UserRepresentation> resetUserPasswordIdentity(String email) {
        Keycloak keycloak = keycloakToken.getInstance();
        return keycloak.realm(realm).users().searchByEmail(email, true).stream().findFirst();
    }
    public List<UserRepresentation> getUserIdentities(List<String> usernames) {
        Keycloak keycloak = keycloakToken.getInstance();
        List<UserRepresentation> userRepresentations = new ArrayList<>();

        // Iterate through each username and query Keycloak for user details
        for (String username : usernames) {
            Optional<UserRepresentation> user = keycloak.realm(realm).users().searchByUsername(username, true).stream().findFirst();
            if (user.isPresent()) {
                userRepresentations.add(user.get());
            }
        }

        return userRepresentations;
    }

    /**
     * Updates the user with the given username in Keycloak.
     *
     * @param userUsernameToUpdate the username of the user to update
     * @param userDto              the updated user information
     * @throws EntityNotFoundException if the user with the given username is not found in Keycloak
     */
    public void updateUser(String userUsernameToUpdate, UserDto userDto) {
        Keycloak keycloak = keycloakToken.getInstance();
        RealmResource realmResource = keycloak.realm(realm);
        Optional<UserRepresentation> keycloakUser = getUserIdentity(userUsernameToUpdate).stream().findFirst();
        UserRepresentation keycloakUserToUpdate = keycloakUser.orElseThrow(() -> new EntityNotFoundException("Keycloak keycloakUser with userUsernameToUpdate  " + userUsernameToUpdate + " not found"));
        UserResource userResource = realmResource.users().get(keycloakUserToUpdate.getId());
        UserRepresentation userRepresentation = userMapper.toUserRepresentation(userDto);
        userRepresentation.setId(keycloakUserToUpdate.getId());
        if (userDto.getPassword() != null) {
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(userDto.getPassword());
            userRepresentation.setCredentials(Collections.singletonList(passwordCred));
        }
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("phone", Collections.singletonList(userDto.getPhone()));
        if (keycloakUserToUpdate.getAttributes() != null) {
            Map<String, List<String>> userAttributes = keycloakUserToUpdate.getAttributes();
            if (userAttributes.get(CUSTOMER_ACCOUNT_ID) != null) {
                String customerAccountId = userAttributes.get(CUSTOMER_ACCOUNT_ID).get(0);
                attributes.put(CUSTOMER_ACCOUNT_ID, Collections.singletonList(customerAccountId));
            }
        }
        userRepresentation.setAttributes(attributes);

        userResource.update(userRepresentation);
    }
}
