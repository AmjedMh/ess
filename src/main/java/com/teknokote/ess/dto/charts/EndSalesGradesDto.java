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
public class EndSalesGradesDto {
    private String fuelGrade;
    private BigDecimal endAmount;
    private BigDecimal endVolume;

    public EndSalesGradesDto(String fuelGrade, BigDecimal endAmount, BigDecimal endVolume) {
        this.fuelGrade = fuelGrade;
        this.endAmount = endAmount;
        this.endVolume = endVolume;
    }
}
