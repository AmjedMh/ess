package com.teknokote.ess.core.dao.requests;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.core.model.requests.EnumRequestStatus;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.requests.FuelGradePriceChangeRequestDto;

import java.util.List;


public interface FuelGradePriceChangeRequestDao extends BasicDao<Long, FuelGradePriceChangeRequestDto>
{
    List<FuelGradePriceChangeRequestDto> findExecutedByStation(Long stationId);

    List<FuelGradePriceChangeRequestDto> findAllByStatusAndReachedPlannedDate(List<EnumRequestStatus> requestStatuses);

    FuelGradeConfigDto toFuelGradeConfig(FuelGradePriceChangeRequestDto fuelGradePriceChangeRequest);

    FuelGradePriceChangeRequestDto findByStation(Long stationId, Long id);

    List<FuelGradePriceChangeRequestDto> findPlannedByStation(Long stationId);
}

