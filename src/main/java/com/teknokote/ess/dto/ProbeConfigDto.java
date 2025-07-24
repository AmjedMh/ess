package com.teknokote.ess.dto;

import lombok.Data;

/**
 *
 */
@Data
public class ProbeConfigDto {
    private Long id;
    private String portId;
    private Long protocol;
    private Long baudRate;
    private String address;
}
