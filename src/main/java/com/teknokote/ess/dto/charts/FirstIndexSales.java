package com.teknokote.ess.dto.charts;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
public class FirstIndexSales {
    private double quantity;
    private BigDecimal totalIndexStart;
}
