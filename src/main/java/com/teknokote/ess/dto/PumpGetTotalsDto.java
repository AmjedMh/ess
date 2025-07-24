package com.teknokote.ess.dto;

import lombok.Data;

@Data
public class PumpGetTotalsDto {
    private Long pump;
    private Long nozzle;
    private Double volume;
    private Double amount;
    private Long transaction;
}
