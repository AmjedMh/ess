package com.teknokote.ess.dto.shifts;

import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class WorkDayShiftPlanningExecutionDto extends ESSIdentifiedDto<Long>{
    private Long workDayShiftPlanningId;
    private Set<ShiftPlanningExecutionDto> shiftPlanningExecutions;
    @Builder
    public WorkDayShiftPlanningExecutionDto(Long id,Long version,Long workDayShiftPlanningId,Set<ShiftPlanningExecutionDto> shiftPlanningExecutions)
    {
        super(id,version);
        this.workDayShiftPlanningId = workDayShiftPlanningId;
        this.shiftPlanningExecutions = shiftPlanningExecutions;
    }
}
