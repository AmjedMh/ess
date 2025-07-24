package com.teknokote.ess.core.dao;

import com.teknokote.core.dao.ActivatableDao;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.dto.PumpAttendantDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PumpAttendantDao extends ActivatableDao<Long, PumpAttendantDto> {
    Page<PumpAttendantDto> findAllByStation(Long stationId, Pageable pageable);

    Optional<PumpAttendantDto> findByStationAndTag(Long stationId, String tag);

    Optional<PumpAttendantDto> findPumpAttandantByStationAndTag(Long stationId, String tag);

    Optional<PumpAttendant> findByTag(String tag, Long stationId);

    Optional<PumpAttendantDto> findByStationAndMatricule(Long stationId, String matricule);
}

