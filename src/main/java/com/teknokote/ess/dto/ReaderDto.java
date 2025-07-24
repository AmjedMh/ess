package com.teknokote.ess.dto;

import lombok.Data;


@Data
public class ReaderDto {
    private Long id;
    private String port;
    private Long address;
    private Long pumpId;
    private Long protocol;
    private Long baudRate;
}
