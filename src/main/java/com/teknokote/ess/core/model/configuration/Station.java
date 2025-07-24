package com.teknokote.ess.core.model.configuration;

import com.teknokote.core.model.ActivatableEntity;
import com.teknokote.ess.core.model.Country;
import com.teknokote.ess.core.model.organization.CustomerAccount;
import com.teknokote.ess.core.model.organization.EnumAffectationMode;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.model.shifts.WorkDayShiftPlanning;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
public class Station extends ActivatableEntity<Long, User> {
    private static final long serialVersionUID = 4245038558633247428L;
    @Column(nullable = false)
    private String identifier;
    @Column(nullable = false)
    private String name;
    private String address;
    private String city;
    private String phone;
    @Enumerated(EnumType.STRING)
    private EnumAffectationMode modeAffectation;
    private String cordonneesGps;

    /**
     * Contrôleur lié
     */
    @OneToOne(mappedBy = "station", cascade = CascadeType.ALL)
    private ControllerPts controllerPts;
    @ManyToOne(cascade = CascadeType.ALL)
    private Country country;
    @Column(name = "country_id", insertable = false, updatable = false)
    private Long countryId;
    @ManyToOne
    private CustomerAccount creatorAccount;
    @Column(name = "creator_account_id", insertable = false, updatable = false)
    private Long creatorAccountId;
    @ManyToOne
    private CustomerAccount customerAccount;
    @Column(name = "customer_account_id", insertable = false, updatable = false)
    private Long customerAccountId;
    @OneToMany(mappedBy = "station")
    private Set<WorkDayShiftPlanning> workDayShiftPlannings;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Station station = (Station) o;

        if (!name.equals(station.name)) return false;
        return customerAccount.equals(station.customerAccount);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + customerAccount.hashCode();
        return result;
    }
}
