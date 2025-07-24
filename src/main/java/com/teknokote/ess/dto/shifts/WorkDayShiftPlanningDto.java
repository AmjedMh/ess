package com.teknokote.ess.dto.shifts;

import com.teknokote.core.dto.ESSIdentifiedDto;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.shifts.ShiftPlanning;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class WorkDayShiftPlanningDto extends ESSIdentifiedDto<Long>{
    private Long stationId;
    private Long shiftRotationId;
    private LocalDate day;
    private Set<ShiftPlanningDto> shiftPlannings;
    @Builder
    public WorkDayShiftPlanningDto(Long id,Long version,Long stationId,Long shiftRotationId,LocalDate day,Set<ShiftPlanningDto> shiftPlannings)
    {
        super(id,version);
        this.stationId = stationId;
        this.shiftRotationId = shiftRotationId;
        this.day = day;
        this.shiftPlannings=shiftPlannings;
    }
}
