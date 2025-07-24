package com.teknokote.ess.dto.requests;

import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.requests.EnumRequestStatus;
import com.teknokote.ess.core.model.requests.EnumRequestType;
import com.teknokote.ess.dto.UserDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FuelGradePriceChangeRequestDto extends AbstractRequestDto{
    private Double oldPrice;
    private Double newPrice;
    private FuelGrade fuelGrade;
    private Long fuelGradeId;
    @Builder
    public FuelGradePriceChangeRequestDto(Long id, Long version, Long stationId, EnumRequestType requestType, LocalDateTime scheduledDate,LocalDateTime createdDate,
                                          LocalDateTime plannedDate, LocalDateTime executionDate, LocalDateTime lastTrialDate, Long requesterId, UserDto requester, EnumRequestStatus status, Double newPrice, Double oldPrice, FuelGrade fuelGrade, Long fuelGradeId)
    {
        super(id, version, stationId, requestType, scheduledDate,createdDate, plannedDate, executionDate, lastTrialDate, requesterId,requester,status);
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.fuelGradeId=fuelGradeId;
        this.fuelGrade = fuelGrade;
    }

    public void startTrial(){
        this.setStatus(EnumRequestStatus.WAITING_RESPONSE);
        this.setLastTrialDate(LocalDateTime.now());
    }

    public void succeed()
    {
        this.setStatus(EnumRequestStatus.EXECUTED);
        this.setExecutionDate(LocalDateTime.now());
    }

    public void fail()
    {
        this.setStatus(EnumRequestStatus.FAILED);
    }
}
