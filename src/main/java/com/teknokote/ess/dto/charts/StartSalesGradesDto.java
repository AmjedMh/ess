package com.teknokote.ess.dto.charts;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@NoArgsConstructor
public class StartSalesGradesDto {
    private String fuelGrade;
    private BigDecimal startAmount;
    private BigDecimal startVolume;
    private double volume;
    private double amount;

    public StartSalesGradesDto(String fuelGrade, BigDecimal startAmount, BigDecimal startVolume, double volume, double amount) {
        this.fuelGrade = fuelGrade;
        this.startAmount = startAmount;
        this.startVolume = startVolume;
        this.volume = volume;
        this.amount = amount;
    }
}
