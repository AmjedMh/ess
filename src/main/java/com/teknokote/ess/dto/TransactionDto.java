package com.teknokote.ess.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDto {

    private LocalDateTime dateTimeStart;
    private Long pump;
    private Long nozzle;
    private BigDecimal volume;
    private float price;
    private BigDecimal amount;
    private Long transaction;
    private String tag;
    private String pumpAttendantName;
    private BigDecimal totalVolume;
    private BigDecimal totalAmount;
    private String fuelGradeName;
    private String devise;

}
