package com.teknokote.ess.dto.shifts;

import com.teknokote.core.dto.ESSIdentifiedDto;
import com.teknokote.ess.dto.PumpAttendantDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AffectedPumpAttendantDto extends ESSIdentifiedDto<Long>{
    private Long pumpAttendantTeamId;
    private Long pumpId;
    private Long pumpAttendantId;
    private PumpAttendantDto pumpAttendant;
    @Builder
    public AffectedPumpAttendantDto(Long id,Long version,Long pumpAttendantTeamId,PumpAttendantDto pumpAttendant, Long pumpId,Long pumpAttendantId)
    {
        super(id,version);
        this.pumpAttendantTeamId = pumpAttendantTeamId;
        this.pumpId = pumpId;
        this.pumpAttendant=pumpAttendant;
        this.pumpAttendantId = pumpAttendantId;
    }
}
