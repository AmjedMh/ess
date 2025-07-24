package com.teknokote.ess.dto.shifts;

import com.teknokote.core.dto.ESSIdentifiedDto;
import com.teknokote.ess.dto.ShiftDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShiftPlanningDto extends ESSIdentifiedDto<Long> {
    private Integer index;
    private Long shiftId;
    private ShiftDto shift;
    private Long stationId;
    private Long shiftRotationId;
    private ShiftRotationDto shiftRotation;
    private Long workDayShiftPlanningId;
    private Long pumpAttendantTeamId;
    private PumpAttendantTeamDto pumpAttendantTeam;
    private Boolean hasExecution;

    @Builder
    public ShiftPlanningDto(Long id, Long version, Integer index, Long stationId, Long shiftId, ShiftDto shift, Long shiftRotationId, ShiftRotationDto shiftRotation,Long workDayShiftPlanningId, Long pumpAttendantTeamId, PumpAttendantTeamDto pumpAttendantTeam, Boolean hasExecution) {
        super(id, version);
        this.index=index;
        this.stationId = stationId;
        this.shiftId = shiftId;
        this.shift = shift;
        this.shiftRotationId = shiftRotationId;
        this.workDayShiftPlanningId=workDayShiftPlanningId;
        this.shiftRotation = shiftRotation;
        this.pumpAttendantTeamId = pumpAttendantTeamId;
        this.pumpAttendantTeam = pumpAttendantTeam;
        this.hasExecution=hasExecution;
    }

    @Override
    public String toString() {
        return "ShiftPlanningDto{" +
                "shiftId=" + shiftId +
                ", pumpAttendantTeamId=" + pumpAttendantTeamId +
                ", stationId=" + stationId +
                ", shiftRotationId=" + shiftRotationId +
                ", workDayShiftPlanningId=" + workDayShiftPlanningId +
                ", id=" + id +
                '}';
    }
}
