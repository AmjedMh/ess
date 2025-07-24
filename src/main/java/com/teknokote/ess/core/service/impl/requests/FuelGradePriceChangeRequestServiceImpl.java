package com.teknokote.ess.core.service.impl.requests;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.ess.core.dao.StationDao;
import com.teknokote.ess.core.dao.requests.FuelGradePriceChangeRequestDao;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.model.requests.EnumRequestStatus;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.service.requests.FuelGradePriceChangeRequestService;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.requests.FuelGradePriceChangeRequestDto;
import com.teknokote.ess.http.logger.EntityActionEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Getter
public class FuelGradePriceChangeRequestServiceImpl extends GenericCheckedService<Long, FuelGradePriceChangeRequestDto> implements FuelGradePriceChangeRequestService
{
    @Autowired
    private ESSValidator<FuelGradePriceChangeRequestDto> validator;
    @Autowired
    private FuelGradePriceChangeRequestDao dao;
    @Autowired
    private StationDao stationDao;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public List<FuelGradePriceChangeRequestDto> findExecutedByStation(Long stationId) {
        return getDao().findExecutedByStation(stationId);
    }

    @Override
    public List<FuelGradePriceChangeRequestDto> findPlannedByStation(Long stationId) {
        return getDao().findPlannedByStation(stationId);
    }

    @Override
    public FuelGradePriceChangeRequestDto findFuelGradePriceChangeRequestByStation(Long stationId, Long id) {
        return getDao().findByStation(stationId,id);
    }

    @Override
    public List<FuelGradePriceChangeRequestDto> findRequestsToRun()
    {
        return getDao().findAllByStatusAndReachedPlannedDate(List.of(EnumRequestStatus.PLANNED,EnumRequestStatus.FAILED));
    }

    @Override
    public FuelGradeConfigDto toFuelGradeConfig(FuelGradePriceChangeRequestDto fuelGradePriceChangeRequest)
    {
        return getDao().toFuelGradeConfig(fuelGradePriceChangeRequest);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FuelGradePriceChangeRequestDto updatePlannedFuelGradeRequest(Long stationId, FuelGradePriceChangeRequestDto dto, HttpServletRequest request)
    {
        Optional<StationDto> stationDto = stationDao.findById(stationId);
        // Check if the stationDto is present
        if (!stationDto.isPresent()) {
            throw new EntityNotFoundException("Station not found with ID: " + stationId);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        FuelGradePriceChangeRequestDto fuelGradePriceChangeRequestDto = super.update(dto);

        // Use the stationDto's value since we've already checked it's present
        EntityActionEvent event = new EntityActionEvent(
                this,
                "update  planned details for fuelGrade "+"'"+fuelGradePriceChangeRequestDto.getFuelGrade().getName()+"'"+ " for station " +"'"+ stationDto.get().getName()+"'" +"for customerAccount "+"'"+stationDto.get().getCustomerAccountName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return fuelGradePriceChangeRequestDto;
    }
}
