package com.teknokote.ess.dto.organization;

import com.teknokote.ess.dto.PumpAttendantDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class DynamicPumpAttendantTeamDto {
    private Long id;
    private String teamName;
    private Long shiftRotationId;
    private Map<Long, PumpAttendantDto> pumpAttendantToPump;

    @Builder
    public DynamicPumpAttendantTeamDto(Long id, String teamName, Map<Long, PumpAttendantDto> pumpAttendantToPump,Long shiftRotationId) {
        this.id = id;
        this.teamName = teamName;
        this.pumpAttendantToPump = pumpAttendantToPump;
        this.shiftRotationId=shiftRotationId;
    }

    public DynamicPumpAttendantTeamDto() {

    }
}
