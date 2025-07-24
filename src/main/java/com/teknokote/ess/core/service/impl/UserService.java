package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ActivatableGenericCheckedService;
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
import com.teknokote.ess.events.publish.cm.exception.ExceptionHandlerUtil;
import com.teknokote.ess.http.logger.EntityActionEvent;
import com.teknokote.ess.http.logger.KeycloakToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.keycloak.email.EmailException;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Getter
public class UserService extends ActivatableGenericCheckedService<Long, UserDto> {
    @Autowired
    private UserValidator validator;
    @Autowired
    private UserDao dao;
    @Autowired
    private CustomerAccountDao customerAccountDao;
    @Autowired
    private CustomerAccountExport customerAccountExport;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailingService mailingService;
    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private KeycloakToken keycloakToken;
    @Autowired
    private UserScopeService userScopeService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Value("${photo.storage.path}")
    private String photoStoragePath;
    static final Map<String, String> resetTokens = new HashMap<>();

    public Optional<UserDto> findByUsername(String name) {
        return getDao().findAllByUsername(name);
    }

    @Transactional
    public UserDto addUser(UserDto userDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUsername = authentication.getName();
        final ESSValidationResult userValidation = getValidator().validateOnCreate(userDto);

        if (userValidation.hasErrors()) {
            throw new ServiceValidationException(userValidation.getMessage());
        }
        userDto.setDateStatusChange(LocalDateTime.now());
        userDto.setUserType(User.EnumUserType.APPLICATION);
        userDto.setUsername(userDto.getUsername().toLowerCase());
        UserDto savedUser = create(userDto, false);
        // Retrieve the functionScopes for the logged-in user
        Optional<UserDto> loggedUserDto = findByUsername(loggedInUsername);
        if (loggedUserDto.isEmpty()) {
            throw new IllegalStateException("Logged-in user not found: " + loggedInUsername);
        }
        List<UserScopeDto> userScopeDtos = userScopeService.findByFunctionalScopeAndUser(EnumFunctionalScope.GLOBAL, loggedUserDto.get().getId());
        // Create UserScopes and add them to the user
        Set<UserScopeDto> userScopes = userScopeDtos.stream()
                .map(scopeDto -> UserScopeDto.builder()
                        .scope(scopeDto.getScope())
                        .relatedUser(savedUser)
                        .customerAccountId(scopeDto.getCustomerAccountId())
                        .stationId(scopeDto.getStationId())
                        .pumpId(scopeDto.getPumpId())
                        .scopeFunctions(scopeDto.getScopeFunctions())
                        .build())
                .collect(Collectors.toSet());
        userScopeService.create(userScopes.stream().toList().get(0));
        userDto.setUserScopes(userScopes);
        userDto.setId(savedUser.getId());
        UserDto updated = update(userDto);
        Optional<CustomerAccountDto> customerAccountDto = customerAccountDao.findById(userDto.getCustomerAccountId());
        if (customerAccountDto.isPresent()) {
            CustomerAccountDto customerAccount = customerAccountDto.get();
            if (customerAccount.isCardManager() && customerAccount.isExported()) {
                Optional<CMSupplierDto> supplierDto = customerAccountExport.exportedCustomerAccount(String.valueOf(customerAccount.getId()));
                supplierDto.ifPresent(supplier -> {
                    try {
                        customerAccountExport.userCreated(supplier.getId(), userDto);
                    } catch (Exception e) {
                        ExceptionHandlerUtil.handleException(e, "Card Manager");
                    }
                });
            }
        }
        keycloakService.createUser(userDto);
        return updated;
    }
    @Transactional
    public UserDto updateUser(UserDto userDto) {
        final UserDto existingUser = checkedFindById(userDto.getId());
        UserDto user = update(userDto);
        Optional<CustomerAccountDto> customerAccountDto = customerAccountDao.findById(user.getCustomerAccountId());
        if (customerAccountDto.isPresent()) {
            CustomerAccountDto customerAccount = customerAccountDto.get();
            if (customerAccount.isCardManager() && customerAccount.isExported()) {
                Optional<CMSupplierDto> supplierDto = customerAccountExport.exportedCustomerAccount(String.valueOf(customerAccount.getId()));
                if (supplierDto.isPresent()) {
                    userDto.setCustomerAccountId(existingUser.getCustomerAccountId());
                    customerAccountExport.userUpdated(supplierDto.get().getId(), userDto);
                }
            }
        }
        keycloakService.updateUser(existingUser.getUsername(), userDto);
        return user;
    }

    @Transactional
    public UserDto updateUserContact(UserDto userDto) {
        Optional<UserDto> userDtoToUpdate = findByUsername(userDto.getUsername());
        if (userDtoToUpdate.isPresent()) {
            UserDto user = userDtoToUpdate.get();
            keycloakService.updateUser(user.getUsername(), userDto);
            userDto.setId(user.getId());
        }
        return update(userDto);
    }

    public Optional<UserDto> userDetails(Long userId) {
        final UserDto userDto = checkedFindById(userId);
        Optional<UserRepresentation> userIdentity = keycloakService.getUserIdentity(userDto.getUsername());

        if (userIdentity.isPresent()) {
            userMapper.enrichDtoFromUserRepresentation(userIdentity.get(), userDto);
            return Optional.of(userDto);
        }else {
            return Optional.empty();
        }
    }

    public Optional<UserDto> userProfile(String username) {
        return findByUsername(username).map(user -> {
            keycloakService.getUserIdentity(user.getUsername().toLowerCase())
                    .ifPresent(identity -> userMapper.enrichDtoFromUserRepresentation(identity, user));
            return user;
        });
    }

    public List<UserDto> listUsersOnCustomerAccounts(List<CustomerAccountDto> customerAccounts) {
        List<UserDto> userDtos = customerAccounts.stream()
                .flatMap(ca -> ca.getAttachedUsers().stream())
                .collect(Collectors.toList());

        List<String> usernames = userDtos.stream()
                .map(userDto -> userDto.getUsername().toLowerCase())
                .collect(Collectors.toList());

        List<UserRepresentation> userRepresentations = keycloakService.getUserIdentities(usernames);

        userRepresentations.forEach(userRepresentation -> {
            UserDto dtoToEnrich = userDtos.stream()
                    .filter(dto -> dto.getUsername().equalsIgnoreCase(userRepresentation.getUsername()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("User not found: " + userRepresentation.getUsername()));
            userMapper.enrichDtoFromUserRepresentation(userRepresentation, dtoToEnrich);
        });

        return userDtos;
    }

    public List<FunctionDto> findUserScopeFunctionsByFunctionalScope(EnumFunctionalScope scope, Long userId) {
        final List<UserScopeDto> userScopes = userScopeService.findByFunctionalScopeAndUser(scope, userId);
        return userScopes.stream().map(UserScopeDto::getScopeFunctions).flatMap(Collection::stream).toList();
    }

    public String generateResetToken(String email) {
        String token = UUID.randomUUID().toString();
        resetTokens.put(token, email);
        return token;
    }

    public String verifyResetToken(String resetToken) {
        String email = resetTokens.get(resetToken);
        if (email != null) {
            resetTokens.remove(resetToken);
            return email;
        }
        throw new RuntimeException("Invalid or expired reset token");
    }

    public void requestPasswordReset(String email) throws EmailException {
        String resetToken = generateResetToken(email);
        if (keycloakService.resetUserPasswordIdentity(email).isPresent()) {
            mailingService.sendResetPassword(email, resetToken);
        } else {
            throw new EntityNotFoundException("user with email:  " + email + " not found");
        }
    }

    public void resetPassword(String resetToken, UserDto userDto) {
        String email = verifyResetToken(resetToken);
        // Reset the password in Keycloak
        keycloakToken.passwordReset(email, userDto);
    }

    public Page<UserDto> findUserByFilter(String name, String creator, String parent, int page, int size) {
        Page<UserDto> userDtoPage = null;

        if (name != null) {
            userDtoPage = getDao().findUserByName(name, PageRequest.of(page, size));
        } else if (creator != null) {
            userDtoPage = getDao().findUserByCreator(creator, PageRequest.of(page, size));
        } else if (parent != null) {
            userDtoPage = getDao().findUserByParent(parent, PageRequest.of(page, size));
        } else if (name == null && creator == null && parent == null) {
            userDtoPage = getDao().findAllUser(PageRequest.of(page, size));
        } else {
            return null;
        }

        return userDtoPage;
    }

    public UserDto findByEmail(String email) {
        Optional<UserRepresentation> userRepresentation = keycloakToken.findUserByEmail(email);
        return userRepresentation
                .map(getDao().getMapper()::userRepresentationToDto)
                .orElseThrow(() -> new EntityNotFoundException("user with email: " + email + " not found"));
    }

    public LoginResponse impersonateUser(User user, Long targetUserId, String accessToken, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserDto> targetUser = getDao().findById(targetUserId);

        if (targetUser.isEmpty()) {
            throw new ServiceValidationException("User not found");
        }

        if (user.getChildCustomerAccounts().contains(targetUser.get().getCustomerAccountId())) {
            LoginResponse response = keycloakToken.impersonateUser(user, targetUser.get(), accessToken);
            dao.updateLastConnection(targetUser.get().getUsername(), LocalDateTime.now());

            EntityActionEvent event = new EntityActionEvent(
                    this,
                    "Impersonation de l'utilisateur '" + targetUser.get().getUsername() + "'",
                    (User) authentication.getPrincipal(),
                    request
            );
            eventPublisher.publishEvent(event);

            return response;
        }

        throw new ServiceValidationException("Impersonation not allowed");
    }

    public Optional<UserDto> findByIdentifier(String masterUserName) {
        return getDao().findByIdentifier(masterUserName);
    }
}
