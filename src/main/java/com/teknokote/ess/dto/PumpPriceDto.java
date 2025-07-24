package com.teknokote.ess.dto;

import lombok.Data;

@Data
public class PumpPriceDto {

    private Long pump;
    private double[] price;
    private String user;
}
