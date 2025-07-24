package com.teknokote.ess.dto;

import lombok.Data;

import java.util.List;

@Data
public class TransactionFilterDto {
    private List<Long> pumpAttendantIds;
    private List<Long> pumpIds;
    private List<Long> fuelGradeIds;
    private RangeFilterDto volume;
    private PeriodFilterDto period;
}
