package com.teknokote.ess.core.model.shifts;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.organization.PumpAttendantTeam;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * ShiftPlanning matérialise le planning des shifts pour:
 * - une station
 * - un shift rotation donné
 * - et une journée donnée
 * <p>
 * A ces éléments, le shiftPlanning associe une équipe (PumpAttendantTeam)
 */
@Entity
@Getter
@Setter
public class ShiftPlanning extends ESSEntity<Long, User> {
    @Serial
    private static final long serialVersionUID = -6649593204984608687L;
    private Integer index;
    @ManyToOne(fetch = FetchType.LAZY)
    private Station station;
    @Column(name = "station_id", insertable = false, updatable = false)
    private Long stationId;
    @ManyToOne
    private Shift shift;
    @Column(name = "shift_id", insertable = false, updatable = false)
    private Long shiftId;
    @ManyToOne
    private ShiftRotation shiftRotation;
    @Column(name = "shift_rotation_id", insertable = false, updatable = false)
    private Long shiftRotationId;
    @ManyToOne(fetch = FetchType.LAZY)
    private WorkDayShiftPlanning workDayShiftPlanning;
    @Column(name = "work_day_shift_planning_id", insertable = false, updatable = false)
    private Long workDayShiftPlanningId;
    @ManyToOne
    private PumpAttendantTeam pumpAttendantTeam;
    @Column(name = "pump_attendant_team_id", insertable = false, updatable = false)
    private Long pumpAttendantTeamId;
    private Boolean hasExecution; // True once the linked execution starts
}
