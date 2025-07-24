package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.core.dao.UserDao;
import com.teknokote.ess.core.dao.mappers.UserMapper;
import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.core.model.organization.Function;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.UserScopeService;
import com.teknokote.ess.dto.FunctionDto;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.dto.UserScopeDto;
import com.teknokote.ess.exceptions.KeycloakUserCreationException;
import com.teknokote.ess.http.logger.KeycloakToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {

    @InjectMocks
    private KeycloakService keycloakService;
    @Mock
    private UserDao userDao;
    @Mock
    private KeycloakToken keycloakToken;
    @Mock
    private Keycloak keycloak;
    @Mock
    private RealmResource realmResource;
    @Mock
    private UserResource userResource;
    @Mock
    private UsersResource usersResource;
    @Mock
    private ClientsResource clientsResource;
    @Mock
    private RolesResource rolesResource;
    @Mock
    private ClientResource clientResource;
    @Mock
    private UserRepresentation userRepresentation;
    @Mock
    private RoleRepresentation impersonationRole;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private RoleScopeResource roleScopeResource;
    @Mock
    private UserScopeService userScopeService;
    @Mock
    private UserDto createUserRequest;
    @Mock
    private Response response;
    @Mock
    private UserMapper userMapper;
    private UserDto userDto;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    private User user;

    @Value("${spring.security.oauth2.client.provider.keycloak.realm}")
    String realm;
    @Value("${spring.security.oauth2.client.provider.keycloak.serverUrl}")
    private String serverUrl;
    @Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-secret}")
    private String clientSecret;
    private String tokenEndpoint;
    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        user = new User();
        user.setUsername("loggedUser");
        userDto = UserDto.builder()
                .username("johndoe")
                .password("securepassword")
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@example.com")
                .username("targetUser")
                .phone("23517353")
                .customerAccountId(null)
                .build();

        lenient().when(keycloak.realm(realm)).thenReturn(realmResource);
        lenient().when(realmResource.users()).thenReturn(usersResource);
        lenient().when(realmResource.clients()).thenReturn(mock(ClientsResource.class));
        lenient().when(realmResource.roles()).thenReturn(mock(RolesResource.class));

        // Use lenient() to avoid unnecessary stubbing errors
        lenient().when(usersResource.searchByUsername(anyString(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(userDto.getUsername());
        lenient().when(usersResource.get("123")).thenReturn(userResource);
        lenient().when(usersResource.create(any())).thenReturn(response);
        lenient().when(response.getStatus()).thenReturn(201);
        lenient().when(response.getLocation()).thenReturn(URI.create("/users/123"));
        lenient().when(userMapper.toUserRepresentation(any())).thenReturn(userRepresentation);
        lenient().when(realmResource.clients()).thenReturn(clientsResource);
        lenient().when(clientsResource.findByClientId("realm-management"))
                .thenReturn(Collections.singletonList(new ClientRepresentation() {{
                    setId("realm-management-client-id");
                }}));
        lenient().when(clientsResource.get("realm-management-client-id")).thenReturn(clientResource);
        lenient().when(clientResource.roles()).thenReturn(rolesResource);
        impersonationRole = new RoleRepresentation();
        impersonationRole.setName("impersonation");
        lenient().when(rolesResource.list()).thenReturn(Collections.singletonList(impersonationRole));
        lenient().when(userResource.roles()).thenReturn(roleMappingResource);
        lenient().when(roleMappingResource.clientLevel(anyString())).thenReturn(roleScopeResource);

        realm = "ess-realm";
        serverUrl = "http://localhost:8080/";
        clientId = "test-client-id";
        clientSecret = "test-client-secret";
        tokenEndpoint = "http://localhost:8080/auth/realms/ess-realm/protocol/openid-connect/token";
    }
    @Test
    void testConfigurationProperties() {
        assertEquals("ess-realm", realm);
        assertEquals("http://localhost:8080/", serverUrl);
        assertEquals("test-client-id", clientId);
        assertEquals("test-client-secret", clientSecret);
        assertEquals("http://localhost:8080/auth/realms/ess-realm/protocol/openid-connect/token", tokenEndpoint);
    }

    @Test
    void createUser_Failure() {
        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userMapper.toUserRepresentation(any(UserDto.class))).thenReturn(userRepresentation);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(400);
        when(keycloakToken.getInstance()).thenReturn(keycloak);

        Exception exception = assertThrows(KeycloakUserCreationException.class, () -> {
            keycloakService.createUser(userDto);
        });

        assertNotNull(exception);
        verify(usersResource, times(1)).create(any(UserRepresentation.class));
    }

    @Test
    void createUser_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getUsername());

        when(keycloakToken.getInstance()).thenReturn(keycloak);

        UserRepresentation result = keycloakService.createUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getUsername(), result.getUsername());
        verify(usersResource, times(1)).create(any(UserRepresentation.class));
    }

    @Test
    void updateUser_Success() {
        // Mock the existing user to be updated
        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setId("123");
        existingUser.setUsername(userDto.getUsername());
        existingUser.setAttributes(Collections.singletonMap("1", Collections.singletonList("1")));
        when(keycloakToken.getInstance()).thenReturn(keycloak);
        when(usersResource.searchByUsername("targetUser", true)).thenReturn(Collections.singletonList(existingUser));
        // Act: Call the updateUser method
        keycloakService.updateUser(userDto.getUsername(), userDto);

        verify(userResource, times(1)).update(any(UserRepresentation.class));
        assertNotNull(userDto);
    }

    @Test
    void testAssignImpersonationRole_whenClientNotFound_throwsException() {

        // Mock the clients() method to return a mock ClientsResource
        ClientsResource clientsResourceMock = mock(ClientsResource.class);
        when(realmResource.clients()).thenReturn(clientsResourceMock);

        // Mock findByClientId to return an empty list (simulating client not found)
        when(clientsResourceMock.findByClientId("realm-management")).thenReturn(Collections.emptyList());

        // Call the method under test and assert that it throws a ServiceValidationException when the client is not found
        assertThrows(ServiceValidationException.class, () -> {
            keycloakService.assignImpersonationRole(keycloak, realmResource, "userId");
        });
    }
    @Test
    void testAssignImpersonationRole_whenRoleNotFound_throwsException() {

        // Mock the clients() method to return a mock ClientsResource
        ClientsResource clientsResourceMock = mock(ClientsResource.class);
        when(realmResource.clients()).thenReturn(clientsResourceMock);

        // Mock findByClientId to return the mock client with the specified clientId
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setId(clientId); // Make sure to set the client ID
        when(clientsResourceMock.findByClientId("realm-management")).thenReturn(Collections.singletonList(clientRepresentation));

        // Mock the get(clientId) method of ClientsResource to return a mock ClientResource
        ClientResource clientResourceMock = mock(ClientResource.class);
        when(clientsResourceMock.get(clientId)).thenReturn(clientResourceMock);

        // Mock the roles() method of ClientResource to return an empty list (to simulate the case where the role is not found)
        RolesResource rolesResourceMock = mock(RolesResource.class);
        when(clientResourceMock.roles()).thenReturn(rolesResourceMock);
        when(rolesResourceMock.list()).thenReturn(Collections.emptyList());

        // Call the method under test and assert that it throws a RuntimeException when the role is not found
        assertThrows(RuntimeException.class, () -> {
            keycloakService.assignImpersonationRole(keycloak, realmResource, "userId");
        });
    }
    @Test
    void testAssignOrCreateUserRole_whenRoleExists() {
        roleScopeResource = mock(RoleScopeResource.class);
        roleMappingResource = mock(RoleMappingResource.class);
        userResource = mock(UserResource.class);

        when(realmResource.users().get("userId")).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName("existingRole");

        when(roleScopeResource.listEffective()).thenReturn(Collections.singletonList(roleRepresentation));
        when(realmResource.roles().list()).thenReturn(Collections.singletonList(roleRepresentation));

        keycloakService.assignOrCreateUserRole(realmResource, "userId", "existingRole");

        verify(roleScopeResource).add(Collections.singletonList(roleRepresentation));
    }
    @Test
    void testAssignOrCreateUserRole_whenRoleDoesNotExist_createsNewRole() {
        // Mock the behavior of the user resource and roles
        when(realmResource.users().get("userId")).thenReturn(userResource);

        // Mock the roles() method to return a non-null object
        roleMappingResource = mock(RoleMappingResource.class);
        when(userResource.roles()).thenReturn(roleMappingResource);

        // Mock the realmLevel() method on RoleMappingResource
        roleScopeResource = mock(RoleScopeResource.class);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        // Mock the effective roles (empty in this case)
        when(roleScopeResource.listEffective()).thenReturn(new ArrayList<>());

        // Mock the roles list in the realmResource
        when(realmResource.roles().list()).thenReturn(new ArrayList<>());

        // Mock the creation of a new role (this should return a RoleResource after creation)
        RoleRepresentation newRole = new RoleRepresentation();
        newRole.setName("newRole");
        when(realmResource.roles().get("newRole")).thenReturn(mock(RoleResource.class));

        // Run the method under test
        keycloakService.assignOrCreateUserRole(realmResource, "userId", "newRole");

        // Verify that the create method was called on the roles resource
        verify(realmResource.roles()).create(any(RoleRepresentation.class));

        // Verify that the add method was called on the user's role mapping
        verify(roleScopeResource).add(anyList());
    }
    @Test
    void testGetUserIdentity_success() {
        String username = "testUser";
        userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(username);
        userRepresentation.setEnabled(true);
        when(keycloakToken.getInstance()).thenReturn(keycloak);

        // Properly stub the searchByUsername method
        when(usersResource.searchByUsername(eq(username), eq(true)))
                .thenReturn(Collections.singletonList(userRepresentation));

        // Call the method under test
        Optional<UserRepresentation> result = keycloakService.getUserIdentity(username);

        // Debugging
        System.out.println("Users returned: " + usersResource.searchByUsername(username, true));

        // Assertions
        assertTrue(result.isPresent(), "Expected user representation to be present but it was not.");
        assertEquals(username, result.get().getUsername());
    }
    @Test
    void testSearchUserByEmail() {
        // Arrange
        List<UserRepresentation> mockUsers = new ArrayList<>();
        mockUsers.add(new UserRepresentation());

        when(usersResource.searchByEmail(anyString(), anyBoolean())).thenReturn(mockUsers);

        // Act
        List<UserRepresentation> users = usersResource.searchByEmail("test@example.com", true);

        // Assert
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }
    @Test
    void testResetUserPasswordIdentity_success() {
        // Given
        String email = "test@example.com";
        userRepresentation = new UserRepresentation();
        userRepresentation.setEmail(email);

        // Always mock the realm method to return the realmResource when called with any string
        when(usersResource.searchByEmail(email, true)).thenReturn(Collections.singletonList(userRepresentation));
        when(keycloakToken.getInstance()).thenReturn(keycloak);
        // When
        Optional<UserRepresentation> result = keycloakService.resetUserPasswordIdentity(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
    }
    @Test
    void testGetUserIdentities() {
        // Set up test data
        List<String> usernames = Arrays.asList("user1", "user2");
        UserRepresentation user1 = new UserRepresentation();
        user1.setUsername("user1");
        UserRepresentation user2 = new UserRepresentation();
        user2.setUsername("user2");

        // Mock method calls
        when(keycloakToken.getInstance()).thenReturn(keycloak);

        when(usersResource.searchByUsername("user1", true)).thenReturn(Collections.singletonList(user1));
        when(usersResource.searchByUsername("user2", true)).thenReturn(Collections.singletonList(user2));

        // Call the method under test
        List<UserRepresentation> result = keycloakService.getUserIdentities(usernames);

        // Assertions
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
    }
    @Test
    void testCreateUserWithFunctionScopes() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getUsername());

        // Set customerAccountId to null
        createUserRequest.setCustomerAccountId(null);
        when(keycloakToken.getInstance()).thenReturn(keycloak);

        // Mock the userDao to return a logged user
        UserDto loggedUserDto = UserDto.builder().id(1L).build();
        lenient().when(userDao.findAllByUsername("testuser")).thenReturn(Optional.of(loggedUserDto));

        // Mock the userScopeService to return some function scopes
        FunctionDto functionDto1 = FunctionDto.builder().build();
        functionDto1.setCode(Function.EnumFunction.MANAGE_USERS);
        FunctionDto functionDto2 = FunctionDto.builder().build();
        functionDto2.setCode(Function.EnumFunction.MANAGE_ATTACHED_USERS);

        Set<FunctionDto> functionSet = new HashSet<>();
        functionSet.add(functionDto1);
        functionSet.add(functionDto2);

        UserScopeDto userScopeDto = UserScopeDto.builder().build();
        userScopeDto.setScopeFunctions(functionSet);

        lenient().when(userScopeService.findByFunctionalScopeAndUser(eq(EnumFunctionalScope.GLOBAL), eq(loggedUserDto.getId())))
                .thenReturn(Collections.singletonList(userScopeDto));

        // Call the method (createUser in this case)
        keycloakService.createUser(createUserRequest);

        verify(userMapper).toUserRepresentation(createUserRequest);
    }

    @Test
    void testCreateUserWithNoFunctionScopes() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getUsername());

        // Set customerAccountId to null
        createUserRequest.setCustomerAccountId(null);
        when(keycloakToken.getInstance()).thenReturn(keycloak);

        // Mock the userDao to return a logged user
        UserDto loggedUserDto = UserDto.builder().id(1L).build();
        lenient().when(userDao.findAllByUsername("testuser")).thenReturn(Optional.of(loggedUserDto));

        // Mock the userScopeService to return no function scopes
        lenient().when(userScopeService.findByFunctionalScopeAndUser(eq(EnumFunctionalScope.GLOBAL), eq(loggedUserDto.getId())))
                .thenReturn(Collections.emptyList());

        // Call the method (createUser in this case)
        keycloakService.createUser(createUserRequest);

        verify(userMapper).toUserRepresentation(createUserRequest);
    }
}
