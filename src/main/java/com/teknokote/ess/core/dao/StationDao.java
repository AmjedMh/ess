package com.teknokote.ess.core.dao;

import com.teknokote.core.dao.JpaActivatableGenericDao;
import com.teknokote.ess.core.dao.mappers.StationMapper;
import com.teknokote.ess.core.model.Country;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.organization.CustomerAccount;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.repository.StationRepository;
import com.teknokote.ess.dto.StationDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Getter
@Slf4j
public class StationDao extends JpaActivatableGenericDao<Long, User, StationDto, Station> {
    @Autowired
    protected StationMapper mapper;
    @Autowired
    protected StationRepository repository;

    @Override
    protected Station beforeCreate(Station station, StationDto dto) {
        station.setCountry(getEntityManager().getReference(Country.class, dto.getCountryId()));
        station.getControllerPts().setStation(station);
        station.getControllerPts().setUserController(getEntityManager().getReference(User.class, dto.getControllerPts().getUserController().getId()));
        station.setCustomerAccount(getEntityManager().getReference(CustomerAccount.class, dto.getCustomerAccountId()));
        if (dto.getCreatorAccountId() != null)
            station.setCreatorAccount(getEntityManager().getReference(CustomerAccount.class, dto.getCreatorAccountId()));
        return super.beforeCreate(station, dto);
    }

    @Override
    protected Station beforeUpdate(Station entity, StationDto dto) {
        if (Objects.nonNull(dto.getCountryId())) {
            entity.setCountry(getEntityManager().getReference(Country.class, dto.getCountryId()));
        }
        return super.beforeUpdate(entity, dto);
    }

    /**
     * Renvoie la liste des stations à l'état actif
     *
     * @param actif
     * @return
     */
    @Override
    public List<StationDto> findAllByActif(boolean actif) {
        final List<Station> stations = getRepository().findAllByActif(true);
        return stations.stream().map(getMapper()::toDto).toList();
    }

    public List<StationDto> findByStation(String name, Long customerAccountId) {
        return getRepository().findByName(name, customerAccountId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    public int countStations(Long customerAccountId) {
        return getRepository().countStations(customerAccountId);
    }

    public StationDto findByControllerPtsId(String ptsId) {
        return getMapper().toDto(getRepository().findByControllerPts_PtsId(ptsId));
    }

    public Optional<StationDto> findByIdentifier(String identifier) {
        return Optional.ofNullable(getMapper().toDto(getRepository().findByIdentifier(identifier).orElse(null)));
    }
}
