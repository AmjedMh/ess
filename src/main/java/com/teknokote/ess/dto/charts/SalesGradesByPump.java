package com.teknokote.ess.dto.charts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class SalesGradesByPump {
    private Long pumpId;
    private String fuelGrade;
    private double totalSalesParAmount;
}
