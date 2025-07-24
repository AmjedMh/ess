package com.teknokote.ess.dto.charts;

import com.teknokote.ess.core.model.EnumAlertStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertDto {
    private String dateTime;
    private String deviceType;
    private Long deviceNumber;
    private EnumAlertStatus status;
    private String alertDescription;
    private String controllerPtsId;
    @Builder
    public AlertDto(String dateTime, String deviceType, Long deviceNumber,EnumAlertStatus status, String controllerPtsId,String alertDescription) {
        this.dateTime = dateTime;
        this.deviceType = deviceType;
        this.deviceNumber = deviceNumber;
        this.status = status;
        this.controllerPtsId = controllerPtsId;
        this.alertDescription = alertDescription;
    }
}
