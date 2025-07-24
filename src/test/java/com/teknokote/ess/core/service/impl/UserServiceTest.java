package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.ess.authentification.model.LoginResponse;
import com.teknokote.ess.core.dao.CustomerAccountDao;
import com.teknokote.ess.core.dao.UserDao;
import com.teknokote.ess.core.dao.mappers.UserMapper;
import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.UserScopeService;
import com.teknokote.ess.core.service.impl.validators.UserValidator;
import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.FunctionDto;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.dto.UserScopeDto;
import com.teknokote.ess.events.publish.CustomerAccountExport;
import com.teknokote.ess.events.publish.cm.CMSupplierDto;
import com.teknokote.ess.http.logger.KeycloakToken;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserValidator validator;
    @Mock
    private UserDao dao;
    @Mock
    private UserScopeService userScopeService;
    @Mock
    private KeycloakToken keycloakToken;
    @Mock
    private CustomerAccountDao customerAccountDao;
    @Mock
    private CustomerAccountExport customerAccountExport;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ESSValidationResult validationResult;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private KeycloakService keycloakService;
    private  UserDto userDto;
    @Mock
    private Page<UserDto> userDtoPage;
    @Mock
    private List<CustomerAccountDto> customerAccounts;
    @Mock
    private UserRepresentation userRepresentation;
    @BeforeEach
    void setup() {
        userDto = UserDto.builder().build();
        userDto.setId(1L);
        userDto.setUsername("testUser");
        userDto.setCustomerAccountId(1L);

        // Set up authentication in the SecurityContext
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn("loggedInUser");

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }
    @Test
    void testAddUser_success() {
        User loggedInUser = new User();
        loggedInUser.setUsername("testUser");
        loggedInUser.setId(1L);

        UserDto user = UserDto.builder().id(1L).username("test user").build();
        // Mock Security Context
        Authentication authentication = new UsernamePasswordAuthenticationToken(loggedInUser, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(validator.validateOnCreate(user)).thenReturn(validationResult);
        when(validator.validateOnUpdate(user)).thenReturn(validationResult);
        when(validationResult.hasErrors()).thenReturn(false);

        // Mock returns for methods expected to return lists or arrays
        List<UserScopeDto> functionalScopes = new ArrayList<>();
        functionalScopes.add(UserScopeDto.builder().build());

        // Adjust to match expected argument types correctly
        when(userScopeService.findByFunctionalScopeAndUser(EnumFunctionalScope.GLOBAL, 1L)).thenReturn(functionalScopes);

        // Create the userDto to be added
        userDto = UserDto.builder().build();
        userDto.setUsername("testUser");

        when(userService.findByUsername(loggedInUser.getUsername())).thenReturn(Optional.of(user));
        when(userService.getDao().create(user)).thenReturn(user);
        when(userService.getDao().update(user)).thenReturn(user);
        when(keycloakService.createUser(any())).thenReturn(new UserRepresentation());

        // Execute method under test
        UserDto result = userService.addUser(user);

        // Validate results and interactions
        assertNotNull(result);
        assertEquals("test user", result.getUsername());
        verify(userService.getDao()).create(user);
        verify(keycloakService).createUser(user);
    }

    // Cleans up after each test
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    @Test
    void testUpdateUser_success() {
        // Create a valid ESSValidationResult with no errors
        validationResult = new ESSValidationResult();
        // Configuration for the validation mock
        when(validator.validateOnUpdate(any())).thenReturn(validationResult);

        // Mock the DAO behavior
        lenient().when(dao.findById(anyLong())).thenReturn(Optional.of(userDto));
        when(dao.update(any())).thenReturn(userDto);
        doNothing().when(keycloakService).updateUser(any(), any());

        // Execute the update user method
        UserDto result = userService.updateUser(userDto);

        // Assertions to validate the result
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());

        // Verify interactions with the mocks
        verify(dao).update(userDto);
        verify(keycloakService).updateUser(userDto.getUsername(), userDto);
    }
    @Test
    void testRequestPasswordReset_userNotFound(){
        when(keycloakService.resetUserPasswordIdentity(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () -> userService.requestPasswordReset("unknown@example.com"));
        assertEquals("user with email:  unknown@example.com not found", exception.getMessage());
    }
    @Test
    void testImpersonateUser_success() {
        User user = new User();
        user.setChildCustomerAccounts(List.of(1L));
        userDto.setId(2L);

        lenient().when(dao.findById(anyLong())).thenReturn(Optional.of(userDto));
        when(keycloakToken.impersonateUser(any(), any(), any())).thenReturn(new LoginResponse());
        doNothing().when(dao).updateLastConnection(any(), any());

        HttpServletRequest request = mock(HttpServletRequest.class);
        LoginResponse response = userService.impersonateUser(user, 2L, "access-token", request);

        assertNotNull(response);

        // Capture the arguments with ArgumentCaptor
        ArgumentCaptor<String> usernameCaptor = forClass(String.class);
        ArgumentCaptor<LocalDateTime> dateTimeCaptor = forClass(LocalDateTime.class);

        verify(dao).updateLastConnection(usernameCaptor.capture(), dateTimeCaptor.capture());

        // Assertions can now check the captured values
        assertEquals("testUser", usernameCaptor.getValue());
        assertNotNull(dateTimeCaptor.getValue());
    }
    @Test
    void testFindByEmail_success() {
        UserRepresentation userRepresentationMock = new UserRepresentation();

        // Mocking the return values
        when(keycloakToken.findUserByEmail(any())).thenReturn(Optional.of(userRepresentationMock));
        when(dao.getMapper()).thenReturn(userMapper);
        when(userMapper.userRepresentationToDto(any(UserRepresentation.class))).thenReturn(userDto);

        // Execute the method
        UserDto result = userService.findByEmail("test@example.com");

        // Assertions
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
    }

    @Test
    void findUserByFilter_NameFilter() {
        String name = "John Doe";
        int page = 0;
        int size = 10;
        when(userService.getDao().findUserByName(eq(name), any(PageRequest.class))).thenReturn(userDtoPage);

        // Act: Call the method with the name filter
        Page<UserDto> result = userService.findUserByFilter(name, null, null, page, size);

        // Assert: Verify that the correct DAO method was called
        verify(userService.getDao(), times(1)).findUserByName(eq(name), any(PageRequest.class));
        assertEquals(userDtoPage, result);
    }

    @Test
    void findUserByFilter_CreatorFilter() {
        String creator = "admin";
        int page = 0;
        int size = 10;
        when(userService.getDao().findUserByCreator(eq(creator), any(PageRequest.class))).thenReturn(userDtoPage);

        // Act: Call the method with the creator filter
        Page<UserDto> result = userService.findUserByFilter(null, creator, null, page, size);

        // Assert: Verify that the correct DAO method was called
        verify(userService.getDao(), times(1)).findUserByCreator(eq(creator), any(PageRequest.class));
        assertEquals(userDtoPage, result);
    }

    @Test
    void findUserByFilter_ParentFilter() {
        String parent = "parentUser";
        int page = 0;
        int size = 10;
        when(userService.getDao().findUserByParent(eq(parent), any(PageRequest.class))).thenReturn(userDtoPage);

        // Act: Call the method with the parent filter
        Page<UserDto> result = userService.findUserByFilter(null, null, parent, page, size);

        // Assert: Verify that the correct DAO method was called
        verify(userService.getDao(), times(1)).findUserByParent(eq(parent), any(PageRequest.class));
        assertEquals(userDtoPage, result);
    }

    @Test
    void findUserByFilter_NoFilters() {
        int page = 0;
        int size = 10;
        when(userService.getDao().findAllUser(any(PageRequest.class))).thenReturn(userDtoPage);

        Page<UserDto> result = userService.findUserByFilter(null, null, null, page, size);

        // Assert: Verify that the correct DAO method was called
        verify(userService.getDao(), times(1)).findAllUser(any(PageRequest.class));
        assertEquals(userDtoPage, result);
    }

    @Test
    void testListUsersOnCustomerAccounts() {
        CustomerAccountDto customerAccountDto = mock(CustomerAccountDto.class);
        when(customerAccountDto.getAttachedUsers()).thenReturn(Set.of(userDto)); // Mock attached users for the customer account

        when(customerAccounts.stream()).thenReturn(Stream.of(customerAccountDto));


        List<String> usernames = Arrays.asList("testuser");
        when(keycloakService.getUserIdentities(usernames)).thenReturn(Arrays.asList(userRepresentation));

        when(userRepresentation.getUsername()).thenReturn("testuser");

        List<UserDto> result = userService.listUsersOnCustomerAccounts(customerAccounts);

        // Assert: Verify the result and interactions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testUser", result.get(0).getUsername());

        // Verify interactions with the mocks
        verify(keycloakService, times(1)).getUserIdentities(usernames);
        verify(userMapper, times(1)).enrichDtoFromUserRepresentation(userRepresentation, userDto);
    }

    @Test
    void testUpdateUserContact_success() {
        // Arrange
        UserDto updatedUserDto = UserDto.builder().username("testUser").email("newemail@example.com").build();
        UserDto existingUserDto = UserDto.builder().username("testUser").build();
        validationResult = mock(ESSValidationResult.class);

        // Mock behavior for finding user
        when(userService.findByUsername(updatedUserDto.getUsername())).thenReturn(Optional.of(existingUserDto));

        // Mock the validation result
        when(validator.validateOnUpdate(updatedUserDto)).thenReturn(validationResult);
        when(validationResult.hasErrors()).thenReturn(false);

        when(userService.findByUsername(updatedUserDto.getUsername())).thenReturn(Optional.of(existingUserDto));
        doNothing().when(keycloakService).updateUser(existingUserDto.getUsername(), updatedUserDto);
        when(userService.getDao().update(updatedUserDto)).thenReturn(updatedUserDto);

        // Act
        UserDto result = userService.updateUserContact(updatedUserDto);

        // Assert
        assertNotNull(result);
        assertEquals("newemail@example.com", result.getEmail());
        verify(keycloakService).updateUser(existingUserDto.getUsername(), updatedUserDto);
        verify(userService.getDao()).update(updatedUserDto);
    }

    @Test
    void testUserDetails_success() {
        // Arrange
        UserDto userToFetch = UserDto.builder().username("testUser").build();
        when(dao.findById(1L)).thenReturn(Optional.ofNullable(userToFetch));
        when(keycloakService.getUserIdentity(userToFetch.getUsername())).thenReturn(Optional.of(userRepresentation));
        when(userMapper.enrichDtoFromUserRepresentation(userRepresentation, userToFetch)).thenReturn(userToFetch);

        // Act
        Optional<UserDto> result = userService.userDetails(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
        verify(userMapper).enrichDtoFromUserRepresentation(userRepresentation, userToFetch);
    }
    @Test
    void testFindByIdentifier_success() {
        String masterUserName = "testUsername";

        when(dao.findByIdentifier(masterUserName)).thenReturn(Optional.of(userDto));

        // Act
        Optional<UserDto> result = userService.findByIdentifier(masterUserName);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
    }
    @Test
    void testAddUser_ValidationFailure() {
        UserDto user = UserDto.builder().username("testUser").build();
        when(validator.validateOnCreate(user)).thenReturn(validationResult);
        when(validationResult.hasErrors()).thenReturn(true); // Validation failure

        Exception exception = assertThrows(ServiceValidationException.class, () -> userService.addUser(user));
        assertEquals(null, exception.getMessage());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        when(dao.findById(anyLong())).thenReturn(Optional.empty()); // User not found exception case

        Exception exception = assertThrows(EntityNotFoundException.class, () -> userService.updateUser(userDto));
        assertEquals("Entité id(1) non trouvée.", exception.getMessage());
    }
    @Test
    void testImpersonateUser_NotAllowed() {
        User user = new User();
        user.setChildCustomerAccounts(Collections.emptyList());

        Exception exception = assertThrows(ServiceValidationException.class, () -> userService.impersonateUser(user, 2L, "", null));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGenerateResetToken() {
        String email = "test@example.com";
        String token = userService.generateResetToken(email);
        assertNotNull(token);
        assertTrue(userService.resetTokens.containsKey(token));
        assertEquals(email, userService.resetTokens.get(token));
    }

    @Test
    void testVerifyResetToken_ValidToken() {
        String email = "test@example.com";
        String token = userService.generateResetToken(email);
        String returnedEmail = userService.verifyResetToken(token);
        assertEquals(email, returnedEmail);
        assertFalse(userService.resetTokens.containsKey(token));
    }

    @Test
    void testVerifyResetToken_InvalidToken() {
        Exception exception = assertThrows(RuntimeException.class, () -> userService.verifyResetToken("invalid"));
        assertEquals("Invalid or expired reset token", exception.getMessage());
    }
    @Test
    void testAddUser_loggedInUserNotFound() {
        UserDto user = UserDto.builder().username("testUser").build();
        when(validator.validateOnCreate(user)).thenReturn(validationResult);
        when(validationResult.hasErrors()).thenReturn(false);

        // Simulate that the logged-in user cannot be found
        when(userService.findByUsername("loggedInUser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalStateException.class, () -> userService.addUser(user));
        assertEquals("Logged-in user not found: loggedInUser", exception.getMessage());
    }
    @Test
    void testAddUser_customerAccountPresent_and_CardManager() {
        // Arrange
        UserDto newUser = UserDto.builder().customerAccountId(1L).username("newuser").build();
        UserDto loggedInUserDto = UserDto.builder().id(1L).username("loggedInUser").build();

        // Ensure that you are creating a complete CustomerAccountDto.
        CustomerAccountDto customerAccount = CustomerAccountDto.builder()
                .id(1L) // Ensure this ID is set correctly
                .cardManager(true)
                .exported(true)
                .build();

        // Simulate user scope data
        UserScopeDto userScope = UserScopeDto.builder()
                .scope(EnumFunctionalScope.GLOBAL)
                .relatedUser(loggedInUserDto)
                .customerAccountId(customerAccount.getId())
                .stationId(1L)
                .pumpId(1L)
                .scopeFunctions(Set.of(FunctionDto.builder().build()))
                .build();

        validationResult = mock(ESSValidationResult.class);
        ESSValidationResult userValidation = mock(ESSValidationResult.class);

        // Mocking validation results
        when(validator.validateOnUpdate(newUser)).thenReturn(validationResult);
        when(validationResult.hasErrors()).thenReturn(false);
        when(validator.validateOnCreate(newUser)).thenReturn(userValidation);
        when(userValidation.hasErrors()).thenReturn(false);

        // Mocking DAO responses and behaviors
        when(userService.getDao().create(newUser)).thenReturn(newUser);
        when(customerAccountDao.findById(newUser.getCustomerAccountId())).thenReturn(Optional.of(customerAccount));

        // Ensure that exportedCustomerAccount returns the correct response needed before userCreated is invoked.
        when(customerAccountExport.exportedCustomerAccount(String.valueOf(customerAccount.getId())))
                .thenReturn(Optional.of(CMSupplierDto.builder().build()));  // Ensure you return a valid CMSupplierDto

        // Mock the logged-in user retrieval
        when(userService.findByUsername("loggedInUser")).thenReturn(Optional.of(loggedInUserDto));

        // Mock user scopes for the logged-in user
        when(userScopeService.findByFunctionalScopeAndUser(EnumFunctionalScope.GLOBAL, loggedInUserDto.getId()))
                .thenReturn(Collections.singletonList(userScope));

        // Act
        userService.addUser(newUser);

        // Assert
        assertEquals("newuser", newUser.getUsername(),  "The username of the newly created user should match the expected value.");
        assertEquals(newUser.getCustomerAccountId(), Long.valueOf(1), "The customer account ID should match the expected value.");

        // You could also verify that the service's create method was called exactly once
        verify(userService.getDao(), times(1)).create(newUser);
    }
    @Test
    void testAddUser_customerAccountNotCardManager() {
        UserDto newUser = UserDto.builder().customerAccountId(1L).username("newuser").build();
        UserDto loggedInUserDto = UserDto.builder().id(1L).username("loggedInUser").build();

        // Ensure that you are creating a complete CustomerAccountDto.
        CustomerAccountDto customerAccount = CustomerAccountDto.builder()
                .id(1L) // Ensure this ID is set correctly
                .cardManager(false)
                .exported(true)
                .build();

        // Simulate user scope data
        UserScopeDto userScope = UserScopeDto.builder()
                .scope(EnumFunctionalScope.GLOBAL)
                .relatedUser(loggedInUserDto)
                .customerAccountId(customerAccount.getId())
                .stationId(1L)
                .pumpId(1L)
                .scopeFunctions(Set.of(FunctionDto.builder().build()))
                .build();

        validationResult = mock(ESSValidationResult.class);
        ESSValidationResult userValidation = mock(ESSValidationResult.class);

        // Mocking validation results
        when(validator.validateOnUpdate(newUser)).thenReturn(validationResult);
        when(validationResult.hasErrors()).thenReturn(false);
        when(validator.validateOnCreate(newUser)).thenReturn(userValidation);
        when(userValidation.hasErrors()).thenReturn(false);

        // Mocking DAO responses and behaviors
        when(userService.getDao().create(newUser)).thenReturn(newUser);
        when(customerAccountDao.findById(newUser.getCustomerAccountId())).thenReturn(Optional.of(customerAccount));

        // Mock the logged-in user retrieval
        when(userService.findByUsername("loggedInUser")).thenReturn(Optional.of(loggedInUserDto));

        // Mock user scopes for the logged-in user
        when(userScopeService.findByFunctionalScopeAndUser(EnumFunctionalScope.GLOBAL, loggedInUserDto.getId()))
                .thenReturn(Collections.singletonList(userScope));

        // Act
        userService.addUser(newUser);

        // Assert
        assertEquals("newuser", newUser.getUsername(), "The username of the newly created user should match the expected value.");
        assertEquals(newUser.getCustomerAccountId(), Long.valueOf(1), "The customer account ID should match the expected value.");

        verify(userService.getDao(), times(1)).create(newUser);
    }
    @Test
    void testUpdateUser_customerAccountPresent_and_CardManager() {
        // Arrange
        UserDto existingUser = UserDto.builder().customerAccountId(1L).username("existingUser").build();
        UserDto updatedUser = UserDto.builder().id(1L).customerAccountId(1L).username("updatedUser").build();

        CustomerAccountDto customerAccount = CustomerAccountDto.builder()
                .id(1L)
                .cardManager(true)
                .exported(true)
                .build();

        // Corrected to use updatedUser for validation
        when(validator.validateOnUpdate(updatedUser)).thenReturn(validationResult);
        when(validationResult.hasErrors()).thenReturn(false);
        when(dao.findById(updatedUser.getId())).thenReturn(Optional.of(existingUser));
        when(dao.update(updatedUser)).thenReturn(updatedUser);
        when(customerAccountDao.findById(updatedUser.getCustomerAccountId())).thenReturn(Optional.of(customerAccount));
        when(customerAccountExport.exportedCustomerAccount(String.valueOf(customerAccount.getId())))
                .thenReturn(Optional.of(CMSupplierDto.builder().build()));

        // Act
        UserDto resultUser = userService.updateUser(updatedUser);

        // Assert
        assertEquals(updatedUser.getId(), resultUser.getId());
        assertEquals(updatedUser.getUsername(), resultUser.getUsername());
        assertEquals(updatedUser.getCustomerAccountId(), resultUser.getCustomerAccountId());

        // Verifying interactions
        verify(validator).validateOnUpdate(updatedUser);
        verify(dao).findById(updatedUser.getId());
        verify(dao).update(updatedUser);
        verify(customerAccountDao).findById(updatedUser.getCustomerAccountId());
        verify(customerAccountExport).exportedCustomerAccount(String.valueOf(customerAccount.getId()));
    }
    @Test
    void testUserProfile_success() {
        UserDto user = UserDto.builder().build();
        user.setUsername("testUser");

        userRepresentation = new UserRepresentation();

        when(userService.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(keycloakService.getUserIdentity(user.getUsername().toLowerCase())).thenReturn(Optional.of(userRepresentation));

        UserDto result = userService.userProfile(user.getUsername()).orElseThrow();

        assertNotNull(result);
        verify(userMapper).enrichDtoFromUserRepresentation(userRepresentation, user);
    }
    @Test
    void testFindUserScopeFunctionsByFunctionalScope() {
        long userId = 1L;
        EnumFunctionalScope scope = EnumFunctionalScope.GLOBAL;
        UserScopeDto userScope = UserScopeDto.builder().build();
        userScope.setScopeFunctions(Set.of(FunctionDto.builder().build()));

        when(userScopeService.findByFunctionalScopeAndUser(scope, userId)).thenReturn(List.of(userScope));

        List<FunctionDto> result = userService.findUserScopeFunctionsByFunctionalScope(scope, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.isEmpty()); // Check that the resulting scope functions are not empty
    }
    @Test
    void testResetPassword_success() {
        String resetToken = userService.generateResetToken("test@example.com");
        UserDto user = UserDto.builder().build();
        user.setEmail("test@example.com");

        // Mocking the behavior of the Keycloak token reset call
        doNothing().when(keycloakToken).passwordReset(anyString(), any(UserDto.class));

        // Execute the reset
        userService.resetPassword(resetToken, user);

        // Verify that the passwordReset method was called
        verify(keycloakToken).passwordReset("test@example.com", user);
    }

}