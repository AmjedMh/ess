package com.teknokote.ess.dto.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PumpStatusDto {
    private String controllerPtsId;
    private Long pumpId;
    private EnumPumpStatus pumpStatus;
    private List<Long> attachedFuelGrades;
    private List<FuelStatusData> fuelStatusData;
}
