package com.teknokote.ess.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
public class SalesDto {
    BigDecimal totalAmountStart;
    BigDecimal totalAmountEnd;
    private double pumpSales;
    private double allSales;
    private Long pumpId;

    public SalesDto(BigDecimal totalAmountStart,BigDecimal totalAmountEnd,double pumpSales, double allSales, Long pumpId) {
        this.totalAmountStart=totalAmountStart;
        this.totalAmountEnd=totalAmountEnd;
        this.pumpSales = pumpSales;
        this.allSales = allSales;
        this.pumpId = pumpId;
    }
}
