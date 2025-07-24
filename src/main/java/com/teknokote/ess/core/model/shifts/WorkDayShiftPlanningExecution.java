package com.teknokote.ess.core.model.shifts;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Entity
@Getter
@Setter
public class WorkDayShiftPlanningExecution extends ESSEntity<Long, User> {
    @Serial
    private static final long serialVersionUID = -4152966733969752561L;
    @ManyToOne(fetch = FetchType.LAZY)
    private WorkDayShiftPlanning workDayShiftPlanning;
    @Column(name = "work_day_shift_planning_id", insertable = false, updatable = false)
    private Long workDayShiftPlanningId;
    @OneToMany(mappedBy = "workDayShiftPlanningExecution", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderColumn(name = "index")
    private List<ShiftPlanningExecution> shiftPlanningExecutions;
}
