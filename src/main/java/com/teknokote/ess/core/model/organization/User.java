package com.teknokote.ess.core.model.organization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.EssUser;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ess_user")
public class User extends EssUser<Long, User> implements UserDetails {
    private static final long serialVersionUID = -6539633896758753994L;

    public enum EnumUserType {
        APPLICATION, // An application user
        CONTROLLER // A controller user
    }
    @Column(nullable = false)
    private String userIdentifier;
    // Unique name: key for users. Will be the login
    private String username;
    private String photoPath;
    private String tag;
    private String matricule;
    @Enumerated(EnumType.STRING)
    private EnumUserType userType;
    private String subnetMask;
    private boolean changePassword;
    private boolean sendSms;
    private LocalDateTime lastConnectionDate;
    // Peut-être null si l'utilisateur n'est pas lié à un "compte client"
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private CustomerAccount customerAccount;
    @Column(name = "customer_account_id", insertable = false, updatable = false)
    private Long customerAccountId;
    @ManyToOne
    private CustomerAccount creatorAccount;
    @Column(name = "creator_account_id", insertable = false, updatable = false)
    private Long creatorAccountId;
    @OneToMany(mappedBy = "relatedUser")
    private Set<CustomFunctionGroup> customFunctionGroups = new HashSet<>();
    /**
     * Les périmètres au niveau utilisateurs sont vus comme des restrictions.
     * Si pas de périmètre, accès à tout.
     */
    @OneToMany(mappedBy = "relatedUser", cascade = CascadeType.ALL)
    private Set<UserScope> functionalUserScopes = new HashSet<>();

    @Transient
    private List<Long> childCustomerAccounts = new ArrayList<>();

    public void addChildCustomerAccount(Long childId) {
        childCustomerAccounts.add(childId);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userIdentifier, user.userIdentifier) && Objects.equals(username, user.username) && Objects.equals(tag, user.tag) && Objects.equals(matricule, user.matricule);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
