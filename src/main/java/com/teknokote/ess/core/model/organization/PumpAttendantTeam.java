package com.teknokote.ess.core.model.organization;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.shifts.AffectedPumpAttendant;
import com.teknokote.ess.core.model.shifts.ShiftRotation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Set;

/**
 * Une équipe de pompistes dispose d'un nom et une liste de (pompiste,pompe) = AffectedPumpAttendant
 * Un même pompiste peut être affecté à plusieurs équipes
 */
@Entity
@Getter
@Setter
public class PumpAttendantTeam extends ESSEntity<Long, User> {

    @Serial
    private static final long serialVersionUID = 6758831913894427797L;

    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    private Station station;
    @Column(name = "station_id", insertable = false, updatable = false)
    private Long stationId;
    @ManyToOne(fetch = FetchType.LAZY)
    private ShiftRotation shiftRotation;
    @Column(name = "shift_rotation_id", insertable = false, updatable = false)
    private Long shiftRotationId;
    @OneToMany(mappedBy = "pumpAttendantTeam", cascade = CascadeType.ALL)
    private Set<AffectedPumpAttendant> affectedPumpAttendant;
}
