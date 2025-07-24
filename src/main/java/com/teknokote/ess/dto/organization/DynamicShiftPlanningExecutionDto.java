package com.teknokote.ess.dto.organization;

import com.teknokote.ess.core.model.shifts.ShiftExecutionStatus;
import com.teknokote.ess.dto.ShiftDto;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DynamicShiftPlanningExecutionDto {
    private Long id;
    private ShiftDto shift;
    private Long shiftId;
    private Integer shiftIndex;
    private Long shiftPlanningId;
    private Long workDayShiftPlanningExecutionId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private ShiftExecutionStatus status;
    private List<DynamicShiftPlanningExecutionDetailDto> dynamicShiftPlanningExecutionDetail;
    private List<PumpAttendantCollectionSheetDto> pumpAttendantCollectionSheets;
    @Builder
    public DynamicShiftPlanningExecutionDto(Long id,Integer shiftIndex,ShiftDto shift,Long shiftId,LocalDateTime startDateTime,LocalDateTime endDateTime,Long shiftPlanningId,ShiftExecutionStatus status,List<DynamicShiftPlanningExecutionDetailDto> dynamicShiftPlanningExecutionDetail,List<PumpAttendantCollectionSheetDto> pumpAttendantCollectionSheets,Long workDayShiftPlanningExecutionId)
    {
        this.id=id;
        this.shiftIndex=shiftIndex;
        this.shift = shift;
        this.shiftId = shiftId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.shiftPlanningId=shiftPlanningId;
        this.workDayShiftPlanningExecutionId=workDayShiftPlanningExecutionId;
        this.status=status;
        this.pumpAttendantCollectionSheets=pumpAttendantCollectionSheets;
        this.dynamicShiftPlanningExecutionDetail=dynamicShiftPlanningExecutionDetail;
    }
    public DynamicShiftPlanningExecutionDto() {
    }
}
