package com.teknokote.ess.controller.front;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.impl.CustomerAccountService;
import com.teknokote.ess.core.service.impl.KeycloakService;
import com.teknokote.ess.core.service.impl.UserService;
import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.FunctionDto;
import com.teknokote.ess.dto.UserDto;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.email.EmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.USER_ROOT)
public class GeneralUserController {
    @Autowired
    private UserService userService;
    @Autowired
    private CustomerAccountService customerAccountService;
    @Autowired
    private KeycloakService keycloakService;

    @PostMapping(EndPoints.ADD)
    public ResponseEntity<Object> addUser(@RequestBody UserDto dto) {
        try {
            UserDto savedUser = userService.addUser(dto);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (ServiceValidationException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(EndPoints.LIST_BY_FILTER)
    public Page<UserDto> listCustomerAccountsByFilter(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String creator,
            @RequestParam(required = false) String parent,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (size != 25 && size != 50 && size != 100 && size != 200 && size != 500) {
            throw new IllegalArgumentException("Invalid page size. Allowed values are 25, 50, 100, 500.");
        }

        return userService.findUserByFilter(name, creator, parent, page, size);
    }

    @PutMapping(EndPoints.UPDATE)
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto dto) {
        UserDto savedUser = userService.updateUser(dto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @GetMapping(EndPoints.USER_LIST)
    public Page<UserDto> listUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        final List<CustomerAccountDto> childCustomerAccounts = customerAccountService.findChildCustomerAccounts(connectedUser);
        List<UserDto> allUsers = userService.listUsersOnCustomerAccounts(childCustomerAccounts);
        allUsers.sort(Comparator.comparing(UserDto::getCreatedDate, Comparator.nullsFirst(Comparator.naturalOrder())));
        int start = page * size;
        int end = Math.min((start + size), allUsers.size());
        return new PageImpl<>(allUsers.subList(start, end), PageRequest.of(page, size), allUsers.size());
    }

    @GetMapping(EndPoints.LIST_BY_ACTIF)
    public List<UserDto> listUserByActif(@PathVariable boolean actif) {
        return userService.findAllByActif(actif);
    }


    @GetMapping(EndPoints.USER_FUNCTION)
    public List<FunctionDto> listAuthorizedFunctions(@PathVariable Long id, @RequestParam EnumFunctionalScope functionalScope) {
        return userService.findUserScopeFunctionsByFunctionalScope(functionalScope, id);
    }

    @PostMapping(EndPoints.DEACTIVATE)
    public ResponseEntity<UserDto> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivate(id));
    }

    @PostMapping(EndPoints.ACTIVATE)
    public ResponseEntity<UserDto> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.activate(id));
    }

    @GetMapping(EndPoints.USER_INFO)
    public Optional<UserDto> userDetails(@PathVariable Long id) {
        return userService.userDetails(id);
    }

    @PostMapping(EndPoints.FORGET_PASSWORD)
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) throws EmailException {
        userService.requestPasswordReset(email);
        return ResponseEntity.ok("Password reset email sent successfully");
    }

    @PutMapping(EndPoints.RESET_PASSWORD)
    public ResponseEntity<String> resetPassword(@RequestParam String resetToken, @RequestBody UserDto userDto) {
        userService.resetPassword(resetToken, userDto);
        return ResponseEntity.ok("Password reset successfully");
    }

    @GetMapping(EndPoints.GET_BY_USERNAME)
    public ResponseEntity<UserDto> getByUsername(@PathVariable String username) {
        Optional<UserDto> user = userService.findByUsername(username);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(EndPoints.GET_BY_EMAIL)
    public ResponseEntity<UserDto> getByEmail(@PathVariable String email) {
        UserDto user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping(EndPoints.UPDATE_CONTACT)
    public ResponseEntity<UserDto> updateUserContact(@RequestBody UserDto updatedUserInfo) {

        UserDto user = userService.updateUserContact(updatedUserInfo);
        return ResponseEntity.ok(user);
    }

    @GetMapping(EndPoints.PROFILE)
    public ResponseEntity<UserDto> getUserProfile(@RequestParam String username) {
        UserDto userDto = userService.userProfile(username)
                .orElse(null);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @GetMapping(EndPoints.SEARCH)
    public List<CustomerAccountDto> findByName() {
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        final List<CustomerAccountDto> childCustomerAccounts = customerAccountService.findChildCustomerAccounts(connectedUser);
        List<CustomerAccountDto> filteredCustomerAccounts = childCustomerAccounts.stream()
                .filter(customerAccountDto -> !customerAccountDto.getMasterUser().getUsername().equals(connectedUser.getUsername())).toList();
        filteredCustomerAccounts.forEach(customerAccountDto -> {
            UserDto masterUserDetails = keycloakService.getUserIdentity(customerAccountDto.getMasterUser().getUsername())
                    .map(master -> UserDto.builder()
                            .id(customerAccountDto.getMasterUser().getId())
                            .username(master.getUsername())
                            .email(master.getEmail())
                            .firstName(master.getFirstName())
                            .lastName(master.getLastName())
                            .phone(master.getAttributes().get("phone") != null
                                    ? master.getAttributes().get("phone").stream().findFirst().orElse("")
                                    : "")
                            .build())
                    .orElseThrow(() -> new EntityNotFoundException("User with username: " + customerAccountDto.getMasterUser().getUsername() + " not found"));

            customerAccountDto.setMasterUser(masterUserDetails);
        });
        return filteredCustomerAccounts;
    }

    @GetMapping(EndPoints.SEARCH_MASTER_USER)
    public List<UserDto> findAllUserByName(@RequestParam(required = false) String name) {
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        final List<CustomerAccountDto> childCustomerAccounts = customerAccountService
                .findChildCustomerAccounts(connectedUser)
                .stream()
                .filter(CustomerAccountDto::isResaleRight)
                .toList();

        List<UserDto> allUsers = userService.listUsersOnCustomerAccounts(childCustomerAccounts);

        return allUsers.stream()
                .filter(userDto ->
                        !userDto.getUsername().equalsIgnoreCase(connectedUser.getUsername()) &&
                                (name == null || StringUtils.containsIgnoreCase(userDto.getUsername(), name))
                )
                .toList();
    }

}