package com.teknokote.ess.core.model.organization;

import com.teknokote.core.model.ActivatableEntity;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.shifts.AffectedPumpAttendant;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.util.Objects;
import java.util.Set;

/**
 * Entité représentant les pompistes
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PumpAttendant extends ActivatableEntity<Long, User> {

    @Serial
    private static final long serialVersionUID = -6076399768247459226L;

    private String firstName;
    private String lastName;
    private String tag;
    private String address;
    private String matricule;
    private String photo;
    private String phone;
    @ManyToOne(fetch = FetchType.LAZY)
    private Station station;
    @Column(name = "station_id", insertable = false, updatable = false)
    private Long stationId;
    @OneToMany(mappedBy = "pumpAttendant")
    private Set<AffectedPumpAttendant> affectedPumpAttendants;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PumpAttendant that = (PumpAttendant) o;

        if (!firstName.equals(that.firstName)) return false;
        if (!lastName.equals(that.lastName)) return false;
        if (!Objects.equals(tag, that.tag)) return false;
        if (!Objects.equals(matricule, that.matricule)) return false;
        return stationId.equals(that.stationId);
    }

    @Override
    public int hashCode() {
        int result = firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        result = 31 * result + (matricule != null ? matricule.hashCode() : 0);
        result = 31 * result + stationId.hashCode();
        return result;
    }
}
