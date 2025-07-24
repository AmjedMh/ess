package com.teknokote.ess.core.service;

import com.teknokote.core.service.ActivatableEntityService;
import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.PumpAttendantDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public interface PumpAttendantService extends ActivatableEntityService<Long, PumpAttendantDto>, BaseService<Long, PumpAttendantDto> {

    PumpAttendantDto findByStationAndTag(Long stationId, String tag);

    Optional<PumpAttendantDto> findPumpAttandantByStationAndTag(Long stationId, String tag);

    PumpAttendantDto findByStationAndMatricule(Long stationId, String matricule);

    PumpAttendantDto findByIdAndStationId(Long id, Long stationId);

    PumpAttendantDto deactivatePumpAttendant(Long stationId, Long id, HttpServletRequest request);

    PumpAttendantDto activatePumpAttendant(Long stationId, Long id, HttpServletRequest request);
}
