package com.teknokote.ess.core.service.requests;

import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.requests.FuelGradePriceChangeRequestDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface FuelGradePriceChangeRequestService extends BaseService<Long, FuelGradePriceChangeRequestDto>
{
    List<FuelGradePriceChangeRequestDto> findExecutedByStation(Long stationId);
    List<FuelGradePriceChangeRequestDto> findPlannedByStation(Long stationId);
    FuelGradePriceChangeRequestDto findFuelGradePriceChangeRequestByStation(Long stationId,Long id);

   List<FuelGradePriceChangeRequestDto> findRequestsToRun();

   FuelGradeConfigDto toFuelGradeConfig(FuelGradePriceChangeRequestDto fuelGradePriceChangeRequest);
    FuelGradePriceChangeRequestDto updatePlannedFuelGradeRequest(Long stationId, FuelGradePriceChangeRequestDto dto, HttpServletRequest request);
}
