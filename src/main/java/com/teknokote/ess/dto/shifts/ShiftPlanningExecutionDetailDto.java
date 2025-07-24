package com.teknokote.ess.dto.shifts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.dto.ESSIdentifiedDto;
import com.teknokote.ess.core.model.configuration.Nozzle;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.dto.PumpAttendantDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TotalVolume = endIndex - startIndex + tankReturn
 */
@Getter
@Setter
public class ShiftPlanningExecutionDetailDto extends ESSIdentifiedDto<Long> {
    private Integer index;
    private Long shiftPlanningExecutionId;
    private Long pumpAttendantId;
    @JsonIgnore
    private PumpAttendantDto pumpAttendant;
    private Long pumpId;
    private Long pumpIdConf;
    private Long nozzleId;
    private Long nozzleIdConf;
    private String nozzleName;
    private boolean complementPumpAttendant;
    private boolean forced;
    @JsonIgnore
    private Nozzle nozzle;
    @JsonIgnore
    private Pump pump;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private BigDecimal tankReturn;
    private BigDecimal startIndex;
    private BigDecimal endIndex;
    private BigDecimal startIndexAmount;
    private BigDecimal endIndexAmount;
    private BigDecimal totalVolume;
    private BigDecimal totalAmount;

    @Builder
    public ShiftPlanningExecutionDetailDto(Long id, Long version, Integer index, Long shiftPlanningExecutionId, Long pumpAttendantId, PumpAttendantDto pumpAttendant, Long pumpId, Long nozzleId, Nozzle nozzle, Pump pump, String nozzleName, BigDecimal tankReturn, BigDecimal startIndex, BigDecimal endIndex, BigDecimal totalVolume, BigDecimal totalAmount, LocalDateTime startDateTime, LocalDateTime endDateTime, BigDecimal startIndexAmount, BigDecimal endIndexAmount, boolean complementPumpAttendant, Boolean forced, Long pumpIdConf, Long nozzleIdConf) {
        super(id, version);
        this.index = index;
        this.shiftPlanningExecutionId = shiftPlanningExecutionId;
        this.pumpAttendantId = pumpAttendantId;
        this.pumpAttendant = pumpAttendant;
        this.pumpId = pumpId;
        this.pump = pump;
        this.nozzleId = nozzleId;
        this.nozzle = nozzle;
        this.nozzleName = nozzleName;
        this.tankReturn = tankReturn;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.totalVolume = totalVolume;
        this.totalAmount = totalAmount;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.complementPumpAttendant = complementPumpAttendant;
        this.forced = forced;
        this.startIndexAmount = startIndexAmount;
        this.endIndexAmount = endIndexAmount;
        this.pumpIdConf = pumpIdConf;
        this.nozzleIdConf = nozzleIdConf;

    }

    public void startExecution(LocalDateTime startDateTime, BigDecimal startIndex, BigDecimal startIndexAmount) {
        this.startDateTime = startDateTime;
        this.startIndex = startIndex;
        this.startIndexAmount = startIndexAmount;
    }

    public void stopExecution(LocalDateTime endDateTime, BigDecimal endIndex, BigDecimal endIndexAmount) {
        this.endDateTime = endDateTime;
        this.endIndex = endIndex;
        this.endIndexAmount = endIndexAmount;
        this.totalVolume = endIndex.subtract(startIndex.add(tankReturn == null ? BigDecimal.ZERO : tankReturn));
        this.totalAmount = endIndexAmount.subtract(startIndexAmount == null ? BigDecimal.ZERO : startIndexAmount);
    }

    public ShiftPlanningExecutionDetailDto(ShiftPlanningExecutionDetailDto other) {
        super(other.getId(), other.getVersion());
        this.index = other.index;
        this.shiftPlanningExecutionId = other.shiftPlanningExecutionId;
        this.pumpAttendantId = other.pumpAttendantId;
        this.pumpAttendant = other.pumpAttendant;
        this.pumpId = other.pumpId;
        this.pump = other.pump;
        this.nozzleId = other.nozzleId;
        this.nozzle = other.nozzle;
        this.nozzleName = other.nozzleName;
        this.tankReturn = other.tankReturn;
        this.startIndex = other.startIndex;
        this.endIndex = other.endIndex;
        this.totalVolume = other.totalVolume;
        this.totalAmount = other.totalAmount;
        this.startDateTime = other.startDateTime;
        this.endDateTime = other.endDateTime;
        this.complementPumpAttendant = other.complementPumpAttendant;
        this.forced = other.forced;
    }

    public List<ShiftPlanningExecutionDetailDto> cloneOnIntervalsAndApply(Long newPumpAttendantId, LocalDateTime start, LocalDateTime end, BigDecimal tankReturn) {
        List<ShiftPlanningExecutionDetailDto> newExecutions = new ArrayList<>();
        if (start.equals(startDateTime) && end.equals(endDateTime)) return List.of();
        if (start.equals(startDateTime) && end.isBefore(endDateTime)) {
            final ShiftPlanningExecutionDetailDto clone1 = new ShiftPlanningExecutionDetailDto(this);
            clone1.setPumpAttendantId(newPumpAttendantId);
            clone1.setEndDateTime(end);
            clone1.setTankReturn(tankReturn);
            clone1.setForced(Boolean.TRUE);
            newExecutions.add(clone1);
            this.setStartDateTime(end);
            this.setForced(Boolean.TRUE);
        }
        if (start.isAfter(startDateTime) && end.isBefore(endDateTime)) {
            final ShiftPlanningExecutionDetailDto clone1 = new ShiftPlanningExecutionDetailDto(this);
            clone1.setPumpAttendantId(newPumpAttendantId);
            clone1.setStartDateTime(start);
            clone1.setEndDateTime(end);
            clone1.setTankReturn(tankReturn);
            clone1.setForced(Boolean.TRUE);
            newExecutions.add(clone1);
            final ShiftPlanningExecutionDetailDto clone2 = new ShiftPlanningExecutionDetailDto(this);
            clone2.setStartDateTime(end);
            clone2.setTankReturn(tankReturn);
            clone2.setForced(Boolean.TRUE);
            newExecutions.add(clone2);
            this.setEndDateTime(start);
            this.setForced(Boolean.TRUE);
        }
        if (start.isAfter(startDateTime) && end.equals(endDateTime)) {
            final ShiftPlanningExecutionDetailDto clone1 = new ShiftPlanningExecutionDetailDto(this);
            clone1.setPumpAttendantId(newPumpAttendantId);
            clone1.setStartDateTime(start);
            clone1.setTankReturn(tankReturn);
            clone1.setForced(Boolean.TRUE);
            newExecutions.add(clone1);
            this.setEndDateTime(start);
            this.setForced(Boolean.TRUE);
        }
        this.setTankReturn(tankReturn);
        return newExecutions;
    }
}
