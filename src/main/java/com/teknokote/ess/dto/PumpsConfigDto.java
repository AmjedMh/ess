package com.teknokote.ess.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PumpsConfigDto {

    private Long id;
    private String portId;
    private Long protocol;
    private Long baudRate;
    private String address;
}
