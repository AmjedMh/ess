package com.teknokote.ess.authentification.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teknokote.ess.core.model.organization.UserScope;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class LoginResponse {
    @JsonProperty(value = "access_token")
    private String accessToken;
    @JsonProperty(value = "refresh_token")
    private String refreshToken;
    @JsonProperty(value = "expires_in")
    private String expiresIn;
    @JsonProperty(value = "refresh_expires_in")
    private String refreshExpiresIn;
    @JsonProperty(value = "token_type")
    private String tokenType;
    @JsonProperty(value = "customer_account_id")
    private Long customerAccountId;
    @JsonProperty(value = "functional_user_scopes")
    private Set<UserScope> functionalUserScopes;
    @JsonProperty(value = "impersonation_mode")
    private boolean impersonationMode;
    @JsonProperty(value = "original_user_id")
    private Long originalUserId;

}


