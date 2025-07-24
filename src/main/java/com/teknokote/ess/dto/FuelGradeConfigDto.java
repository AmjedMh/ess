package com.teknokote.ess.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FuelGradeConfigDto {

    private Long id;
    private Long idConf;
    private String name;
    private Double price;
    private Double expansionCoefficient;
    private LocalDateTime plannedDate;
    private String reference;
    private LocalDateTime scheduledDate;
}
