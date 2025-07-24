package com.teknokote.ess.core.model.shifts;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class ShiftPlanningExecution extends ESSEntity<Long, User> {
    @Serial
    private static final long serialVersionUID = -817068807557781339L;
    private Integer index;
    @ManyToOne
    private Shift shift;
    @Column(name = "shift_id", insertable = false, updatable = false)
    private Long shiftId;
    @ManyToOne(fetch = FetchType.LAZY)
    private ShiftPlanning shiftPlanning;
    @Column(name = "shift_planning_id", insertable = false, updatable = false)
    private Long shiftPlanningId;
    @ManyToOne(fetch = FetchType.LAZY)
    private WorkDayShiftPlanningExecution workDayShiftPlanningExecution;
    @Column(name = "work_day_shift_planning_execution_id", insertable = false, updatable = false)
    private Long workDayShiftPlanningExecutionId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    @Enumerated(EnumType.STRING)
    private ShiftExecutionStatus status;
    @OneToMany(mappedBy = "shiftPlanningExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "index")
    private List<ShiftPlanningExecutionDetail> shiftPlanningExecutionDetail;
    @OneToMany(mappedBy = "shiftPlanningExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PumpAttendantCollectionSheet> pumpAttendantCollectionSheets;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShiftPlanningExecution that = (ShiftPlanningExecution) o;

        if (!shiftId.equals(that.shiftId)) return false;
        return shiftPlanningId.equals(that.shiftPlanningId);
    }

    @Override
    public int hashCode() {
        int result = shiftId.hashCode();
        result = 31 * result + shiftPlanningId.hashCode();
        return result;
    }
}
