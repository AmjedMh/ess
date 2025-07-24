package com.teknokote.ess.core.model.shifts;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.Nozzle;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ShiftPlanningExecutionDetail extends ESSEntity<Long, User> {
    @Serial
    private static final long serialVersionUID = -36236398107070344L;
    private Integer index;
    @ManyToOne
    private ShiftPlanningExecution shiftPlanningExecution;
    @Column(name = "shift_planning_execution_id", insertable = false, updatable = false)
    private Long shiftPlanningExecutionId;
    // Détails
    @ManyToOne
    private PumpAttendant pumpAttendant;
    @Column(name = "pump_attendant_id", insertable = false, updatable = false)
    private Long pumpAttendantId;
    @ManyToOne(fetch = FetchType.LAZY)
    private Pump pump;
    @Column(name = "pump_id", insertable = false, updatable = false)
    private Long pumpId;
    @ManyToOne(fetch = FetchType.LAZY)
    private Nozzle nozzle;
    @Column(name = "nozzle_id", insertable = false, updatable = false)
    private Long nozzleId;
    private boolean forced;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private BigDecimal tankReturn;
    private BigDecimal startIndex;
    private BigDecimal endIndex;
    private BigDecimal startIndexAmount;
    private BigDecimal endIndexAmount;
    private BigDecimal totalVolume;
    private BigDecimal totalAmount;
}
