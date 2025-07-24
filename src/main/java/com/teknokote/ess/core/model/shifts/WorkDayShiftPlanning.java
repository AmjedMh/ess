package com.teknokote.ess.core.model.shifts;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List;

/**
 * Cette entité représente une journée de travail planinfiée dans une station.
 * Une journée de travail planifiée regroupe les plannings des shifts
 */
@Entity
@Getter
@Setter
public class WorkDayShiftPlanning extends ESSEntity<Long, User> {

    @Serial
    private static final long serialVersionUID = 4572853400500248091L;
    @ManyToOne(fetch = FetchType.LAZY)
    private Station station;
    @Column(name = "station_id", insertable = false, updatable = false)
    private Long stationId;
    @ManyToOne(fetch = FetchType.LAZY)
    private ShiftRotation shiftRotation;
    @Column(name = "shift_rotation_id", insertable = false, updatable = false)
    private Long shiftRotationId;
    @OneToMany(mappedBy = "workDayShiftPlanning", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderColumn(name = "index")
    private List<ShiftPlanning> shiftPlannings;
    private LocalDate day;
}
