package com.teknokote.ess.dto.organization;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DynamicShiftPlanningDto {
    private Long id;
    private LocalDate day;
    private Integer shiftIndex; // index du shift dans la rotation (ordre)
    private String shiftName;
    private Long shiftId;
    private Boolean offDay;
    private String pumpAttendantTeamName;
    private PumpAttendantTeamDto pumpAttendantTeam;
    private String shiftRotationName;
    private Long shiftRotationId;
    private Boolean hasExecution;


    @Builder
    public DynamicShiftPlanningDto(LocalDate day, String shiftName,
                                   String pumpAttendantTeamName,
                                   PumpAttendantTeamDto pumpAttendantTeam,
                                   String shiftRotationName,
                                   Long id,
                                   Long shiftId,
                                   Integer shiftIndex,
                                   Boolean offDay,
                                   Long shiftRotationId,
                                   Boolean hasExecution) {
        this.id=id;
        this.shiftId=shiftId;
        this.day = day;
        this.shiftIndex=shiftIndex;
        this.shiftName = shiftName;
        this.offDay = offDay;
        this.pumpAttendantTeam = pumpAttendantTeam;
        this.pumpAttendantTeamName = pumpAttendantTeamName;
        this.shiftRotationName = shiftRotationName;
        this.shiftRotationId = shiftRotationId;
        this.hasExecution = hasExecution;
    }

    public DynamicShiftPlanningDto() {
    }
}
