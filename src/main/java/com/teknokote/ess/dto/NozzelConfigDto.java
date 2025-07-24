package com.teknokote.ess.dto;

import lombok.Data;
@Data
public class NozzelConfigDto {

    private Long id;
    private Long idConf;
    private String pump;
    private String tank;
    private String fuelGrad;
    private String fuelId;
}
