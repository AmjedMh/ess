package com.teknokote.ess.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PumpTransactionInformationDto {
    private LocalDateTime dateTimeStart;
    private LocalDateTime dateTime;
    private Long pump;
    private Long nozzle;
    private float tcVolume;
    private Double volume;
    private float price;
    private Double amount;
    private Long transaction;
    private BigDecimal totalVolume;
    private BigDecimal totalAmount;
    private String tag;
    private String state;

}
