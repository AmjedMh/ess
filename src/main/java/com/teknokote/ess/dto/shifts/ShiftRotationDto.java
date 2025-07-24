package com.teknokote.ess.dto.shifts;

import com.teknokote.ess.core.model.shifts.EnumPlanificationMode;
import com.teknokote.ess.dto.ShiftDto;
import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ShiftRotationDto extends ESSIdentifiedDto<Long>{
    private String name;
    private LocalDate startValidityDate;
    private LocalDate endValidityDate;
    private Long stationId;
    private Integer nbrOffDays;
    private EnumPlanificationMode planificationMode;
    private List<ShiftDto> shifts;
    @Builder
    public ShiftRotationDto(Long id,Long version,String name,LocalDate startValidityDate,LocalDate endValidityDate,Long stationId,List<ShiftDto> shifts, Integer nbrOffDays,EnumPlanificationMode planificationMode)
    {
        super(id,version);
        this.name = name;
        this.startValidityDate = startValidityDate;
        this.endValidityDate = endValidityDate;
        this.shifts=shifts;
        this.stationId = stationId;
        this.planificationMode=planificationMode;
        this.nbrOffDays=nbrOffDays;
    }

    public ShiftDto firstShift()
    {
        return shifts.get(0);
    }

    public ShiftDto lastShift()
    {
        return shifts.get(shifts.size() - 1);
    }
}
