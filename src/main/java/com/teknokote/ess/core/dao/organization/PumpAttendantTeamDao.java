package com.teknokote.ess.core.dao.organization;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;

import java.util.List;
import java.util.Optional;

public interface PumpAttendantTeamDao extends BasicDao<Long, PumpAttendantTeamDto> {
    PumpAttendantTeamDto findByShiftIdAndShiftRotationId(Long shiftId, Long shiftRotationId);
    Optional<PumpAttendantTeamDto> findByRotationAndName(Long stationId, Long shiftRotationId, String name);

    List<PumpAttendantTeamDto> findByPumpAttendant(Long pumpAttendantId);
    List<PumpAttendantTeamDto> findTeamsByRotationAndPumpAttendant(Long shiftRotationId, Long pumpAttendantId);

    List<PumpAttendantTeamDto> findByStationAndRotation(Long stationId, Long shiftRotationId);

    List<PumpAttendantTeamDto> findAllByStation(Long stationId);

    void deleteForStationAndRotation(Long stationId, Long shiftRotationId);
}

