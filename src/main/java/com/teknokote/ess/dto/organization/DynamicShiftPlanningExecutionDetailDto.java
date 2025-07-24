package com.teknokote.ess.dto.organization;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DynamicShiftPlanningExecutionDetailDto {
    private Long id;
    private Long shiftPlanningExecutionId;
    private String pumpAttendantName;
    private Long pumpAttendantId;
    private Long pumpId;
    private Long pumpIdConf;
    private Long nozzleId;
    private Long nozzleIdConf;
    private String nozzleName;
    private BigDecimal tankReturn;
    private BigDecimal startIndex;
    private BigDecimal endIndex;
    private BigDecimal totalVolume;
    private BigDecimal totalAmount;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    @Builder
    public DynamicShiftPlanningExecutionDetailDto(Long id,Long shiftPlanningExecutionId, String pumpAttendantName,Long pumpAttendantId, Long pumpId,Long pumpIdConf, String nozzleName,Long nozzleId,Long nozzleIdConf, BigDecimal tankReturn, BigDecimal startIndex, BigDecimal endIndex, BigDecimal totalVolume, BigDecimal totalAmount,LocalDateTime startDateTime,LocalDateTime endDateTime) {
        this.id=id;
        this.shiftPlanningExecutionId = shiftPlanningExecutionId;
        this.pumpAttendantName = pumpAttendantName;
        this.pumpAttendantId=pumpAttendantId;
        this.pumpId = pumpId;
        this.pumpIdConf =pumpIdConf;
        this.nozzleId=nozzleId;
        this.nozzleName = nozzleName;
        this.tankReturn = tankReturn;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.totalVolume = totalVolume;
        this.totalAmount = totalAmount;
        this.nozzleIdConf=nozzleIdConf;
        this.startDateTime=startDateTime;
        this.endDateTime=endDateTime;
    }

    public DynamicShiftPlanningExecutionDetailDto() {
    }
}
