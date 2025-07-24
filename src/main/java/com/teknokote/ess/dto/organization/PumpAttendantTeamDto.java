package com.teknokote.ess.dto.organization;

import com.teknokote.core.dto.ESSIdentifiedDto;
import com.teknokote.ess.dto.shifts.AffectedPumpAttendantDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PumpAttendantTeamDto extends ESSIdentifiedDto<Long>{
    private String name;
    private Long stationId;
    private Long shiftRotationId;
    private Set<AffectedPumpAttendantDto> affectedPumpAttendant;
    @Builder
    public PumpAttendantTeamDto(Long id,Long version,Long stationId,Long shiftRotationId,String name,Set<AffectedPumpAttendantDto> affectedPumpAttendant)
    {
        super(id,version);
        this.name = name;
        this.stationId=stationId;
        this.shiftRotationId=shiftRotationId;
        this.affectedPumpAttendant = affectedPumpAttendant;
    }
}
