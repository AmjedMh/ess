package com.teknokote.ess.dto;

import com.teknokote.core.dto.ESSActivatableDto;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class UserDto extends ESSActivatableDto<Long> {
    private String userIdentifier;
    @NotEmpty
    private String username;
    private String firstName;
    private String lastName;
    @Email
    private String email;
    private String password;
    private String role;
    private String phone;
    private String photoPath;
    private String matricule;
    private String tag;
    // le compte créateur de l'utilisateur
    private Long creatorAccountId;
    private String creatorCustomerAccountName;
    // Le compte auquel est rattché l'utilisateur
    private Long customerAccountId;
    private String customerAccountName;
    private String subnetMask;
    private boolean sendSms;
    private boolean changePassword;
    private LocalDateTime lastConnectionDate;
    @Enumerated(EnumType.STRING)
    private User.EnumUserType userType;
    private Set<FunctionDto> scopeFunctions;
    private Set<UserScopeDto> userScopes;
    private LocalDateTime createdDate;


    @Builder
    public UserDto(Long id, Long version, LocalDateTime createdDate, String username, boolean actif, boolean changePassword, boolean sendSms,
                   String subnetMask, LocalDateTime dateStatusChange, String photoPath, String tag, String matricule,String userIdentifier,
                   String firstName, String lastName, String email, Long customerAccountId, String customerAccountName,
                   String creatorCustomerAccountName, Long creatorAccountId, Set<FunctionDto> scopeFunctions, Set<UserScopeDto> userScopes,
                   String phone, User.EnumUserType userType, String password, LocalDateTime lastConnectionDate) {
        super(id, version, actif, dateStatusChange);
        this.userIdentifier=userIdentifier;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.photoPath = photoPath;
        this.tag = tag;
        this.matricule = matricule;
        this.creatorAccountId = creatorAccountId;
        this.customerAccountId = customerAccountId;
        this.creatorCustomerAccountName = creatorCustomerAccountName;
        this.userType = userType;
        this.scopeFunctions = scopeFunctions;
        this.userScopes = userScopes;
        this.password = password;
        this.subnetMask = subnetMask;
        this.sendSms = sendSms;
        this.changePassword = changePassword;
        this.customerAccountName = customerAccountName;
        this.lastConnectionDate = lastConnectionDate;
        this.createdDate = createdDate;
    }

    public static UserDto of(String username) {
        final UserDto userDto = UserDto.builder().build();
        userDto.setUsername(username);
        return userDto;
    }
}
