package com.teknokote.ess.core.model.organization;

import com.teknokote.core.model.ActivatableEntity;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.shifts.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entité pour la notion de "comptes client".
 * Un compte client regroupe des stations et des utilisateurs attachés.
 * Pour chaque compte client est associé un utilisateur "master" (par défaut, de même nom) qui a tous les droits
 * sur toutes ses stations et sur les utilisateurs attachés.
 * Le "masterUser" peut:
 * - gérer et ajouter des stations
 * - gérer et ajouter des utilisateurs liés.
 * Un compte client avec ACCESS_DISABLED n'autorise pas l'accès à ses utilisateurs. Les données des stations continuent à être intégrées.
 * (ACCESS_DISABLED = état par défaut)
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CustomerAccount extends ActivatableEntity<Long, User> {
    private static final long serialVersionUID = 8224234998640362111L;
    @Column(nullable = false)
    private String identifier;
    @Column(nullable = false)
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private EnumCustomerAccountStatus status;
    private String city;
    private String phone;
    /**
     * le parent du revendeur = le revendeur qui l'a créé.
     * L'administrateur général appartient au revendeur parent ROOT par exemple
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private CustomerAccount parent;
    @Column(name = "parent_id", insertable = false, updatable = false)
    private Long parentId;
    @ManyToOne(fetch = FetchType.LAZY)
    private CustomerAccount creatorAccount;
    @Column(name = "creator_account_id", insertable = false, updatable = false)
    private Long creatorAccountId;
    /**
     * Liste des comptes clients attachés créés par ce revendeur
     */
    @OneToMany(mappedBy = "parent")
    private Set<CustomerAccount> attachedCustomerAccounts = new HashSet<>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private User masterUser;
    @Column(name = "master_user_id", insertable = false, updatable = false)
    private Long masterUserId;
    @OneToMany(mappedBy = "customerAccount", cascade = {CascadeType.PERSIST})
    private Set<Station> stations = new HashSet<>();
    @OneToMany(mappedBy = "customerAccount")
    private Set<User> attachedUsers = new HashSet<>();
    @OneToMany(mappedBy = "customerAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentMethod> paymentMethods;

    /**
     * Utilisateur créateur du customerAccount
     */
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private User creatorUser;
    @Column(name = "creator_user_id", insertable = false, updatable = false)
    private Long creatorUserId;
    /**
     * Droit de revente du servicce
     */
    private boolean resaleRight;
    private boolean cardManager;
    private boolean exported;
    private LocalDateTime plannedExportDate;
    private LocalDateTime scheduledDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomerAccount that = (CustomerAccount) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Un "compte client" est créé à l'état ACCESS_DISABLED par défaut
     */
    public static CustomerAccount create(String customerName) {
        return CustomerAccount.builder().name(customerName).status(EnumCustomerAccountStatus.ACCESS_DISABLED).build();
    }

}
