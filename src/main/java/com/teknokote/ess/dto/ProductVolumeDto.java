package com.teknokote.ess.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProductVolumeDto {
    private LocalDateTime dateTime;
    private Double productVolume;
}
