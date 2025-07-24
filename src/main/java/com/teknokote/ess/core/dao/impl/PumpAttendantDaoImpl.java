package com.teknokote.ess.core.dao.impl;

import com.teknokote.core.dao.JpaActivatableGenericDao;
import com.teknokote.ess.core.dao.PumpAttendantDao;
import com.teknokote.ess.core.dao.mappers.PumpAttendantMapper;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.repository.PumpAttendantRepository;
import com.teknokote.ess.dto.PumpAttendantDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Getter
@Setter
public class PumpAttendantDaoImpl extends JpaActivatableGenericDao<Long, User, PumpAttendantDto, PumpAttendant> implements PumpAttendantDao {
    @Autowired
    private PumpAttendantMapper mapper;
    @Autowired
    private PumpAttendantRepository repository;

    public Page<PumpAttendantDto> findAllByStation(Long stationId, Pageable pageable) {
        return getRepository().findAllByStation(stationId, pageable).map(getMapper()::toDto);
    }


    @Override
    public Optional<PumpAttendantDto> findByStationAndTag(Long stationId, String tag) {
        return getRepository().findByStationAndTag(stationId, tag).map(getMapper()::toDto);
    }

    @Override
    public Optional<PumpAttendantDto> findPumpAttandantByStationAndTag(Long stationId, String tag) {
        return getRepository().findByStationAndTag(stationId, tag).map(getMapper()::toDto);
    }

    public Optional<PumpAttendant> findByTag(String tag, Long stationId) {
        return getRepository().findByTag(tag, stationId);
    }

    @Override
    public Optional<PumpAttendantDto> findByStationAndMatricule(Long stationId, String matricule) {
        return getRepository().findByStationAndMatricule(stationId, matricule).map(getMapper()::toDto);
    }

    @Override
    protected PumpAttendant beforeCreate(PumpAttendant pumpAttendant, PumpAttendantDto dto) {
        pumpAttendant.setDateStatusChange(LocalDateTime.now());
        pumpAttendant.setActif(dto.getActif());
        pumpAttendant.setStation(getEntityManager().getReference(Station.class, dto.getStationId()));
        return super.beforeCreate(pumpAttendant, dto);
    }
}
