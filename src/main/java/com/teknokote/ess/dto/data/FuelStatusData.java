package com.teknokote.ess.dto.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FuelStatusData {
    private Long fuelGradeId;
    private String fuelGradeName;
    private Double volume;
    private Double amount;
    private Double price;
    private BigDecimal totalVolume;
    private BigDecimal totalAmount;
    private BigDecimal todaySalesVolume;
    private BigDecimal todaySalesAmount;
    private BigDecimal initialTotalVolume;
    private BigDecimal initialTotalAmount;
}
