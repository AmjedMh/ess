package com.teknokote.ess.dto.charts;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SumFuelGradesByPumpDto {

    private String nameF;
    private String dateF;
    private Double sumF;
}
