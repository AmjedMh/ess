package com.teknokote.ess.core.service.organization;

import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.organization.DynamicPumpAttendantTeamDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface PumpAttendantTeamService extends BaseService<Long, PumpAttendantTeamDto> {
    List<PumpAttendantTeamDto> findByStation(Long stationId);

    PumpAttendantTeamDto addTeam(Long stationId, PumpAttendantTeamDto pumpAttendantTeamDto, HttpServletRequest request);

    PumpAttendantTeamDto updateTeam(Long stationId, PumpAttendantTeamDto pumpAttendantTeamDto,HttpServletRequest request);

    List<DynamicPumpAttendantTeamDto> getDynamicPumpAttendantTeam(Long stationId);

    List<DynamicPumpAttendantTeamDto> getDynamicPumpAttendantTeams(Long stationId, Long shiftRotationId);

    List<PumpAttendantTeamDto> findByPumpAttendant(Long pumpAttendantId);

    List<PumpAttendantTeamDto> findTeamsByRotationAndPumpAttendant(Long shiftRotationId, Long pumpAttendantId);

    List<PumpAttendantTeamDto> findByRotation(Long stationId, Long shiftRotationId);
}
