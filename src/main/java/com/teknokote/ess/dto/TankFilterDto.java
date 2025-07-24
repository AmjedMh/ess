package com.teknokote.ess.dto;

import lombok.Data;

import java.util.List;

@Data
public class TankFilterDto {
    private List<Long> tank;
    private PeriodFilterDto period;

}
