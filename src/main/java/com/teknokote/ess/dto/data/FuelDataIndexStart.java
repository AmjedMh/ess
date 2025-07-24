package com.teknokote.ess.dto.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FuelDataIndexStart {
    private BigDecimal initialTotalVolume;
    private BigDecimal initialTotalAmount;

    public FuelDataIndexStart(BigDecimal initialTotalVolume, BigDecimal initialTotalAmount) {
        this.initialTotalVolume = initialTotalVolume;
        this.initialTotalAmount = initialTotalAmount;
    }
}
