package com.teknokote.ess.core.dao.impl.requests;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.requests.FuelGradePriceChangeRequestMapper;
import com.teknokote.ess.core.dao.requests.FuelGradePriceChangeRequestDao;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.model.requests.EnumRequestStatus;
import com.teknokote.ess.core.model.requests.FuelGradePriceChangeRequest;
import com.teknokote.ess.core.repository.requests.FuelGradePriceChangeRequestRepository;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.requests.FuelGradePriceChangeRequestDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Getter
@Setter
public class FuelGradePriceChangeRequestDaoImpl extends JpaGenericDao<Long, FuelGradePriceChangeRequestDto, FuelGradePriceChangeRequest> implements FuelGradePriceChangeRequestDao
{
    @Autowired
    private FuelGradePriceChangeRequestMapper mapper;
    @Autowired
    private FuelGradePriceChangeRequestRepository repository;

    @Override
    public List<FuelGradePriceChangeRequestDto> findExecutedByStation(Long stationId) {
        return getRepository().findExecutedByStation(stationId).stream().map(mapper::toDto).collect(Collectors.toList());
    }
    @Override
    protected FuelGradePriceChangeRequest beforeCreate(FuelGradePriceChangeRequest fuelGradePriceChangeRequest, FuelGradePriceChangeRequestDto dto) {
        if (dto.getFuelGradeId()!=null){
            fuelGradePriceChangeRequest.setFuelGrade(getEntityManager().getReference(FuelGrade.class,dto.getFuelGradeId()));
        }
        if (dto.getStationId()!=null){
            fuelGradePriceChangeRequest.setStation(getEntityManager().getReference(Station.class,dto.getStationId()));
        }
        if (dto.getRequesterId()!=null){
            fuelGradePriceChangeRequest.setRequester(getEntityManager().getReference(User.class,dto.getRequesterId()));
        }
        return super.beforeCreate(fuelGradePriceChangeRequest,dto);
    }

    @Override
    public List<FuelGradePriceChangeRequestDto> findAllByStatusAndReachedPlannedDate(List<EnumRequestStatus> requestStatuses)
    {
        return getRepository().findAllByStatusAndReachedPlannedDate(requestStatuses).stream().map(getMapper()::toDto).toList();
    }

    @Override
    public FuelGradeConfigDto toFuelGradeConfig(FuelGradePriceChangeRequestDto fuelGradePriceChangeRequest)
    {
        return getMapper().toFuelGradeConfig(fuelGradePriceChangeRequest);
    }

    @Override
    public FuelGradePriceChangeRequestDto findByStation(Long stationId, Long id) {
        return getMapper().toDto(getRepository().findByStation(stationId,id));
    }

    @Override
    public List<FuelGradePriceChangeRequestDto> findPlannedByStation(Long stationId) {
        return getRepository().findPlannedByStation(stationId).stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
