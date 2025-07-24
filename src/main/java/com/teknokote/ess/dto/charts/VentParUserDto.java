package com.teknokote.ess.dto.charts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VentParUserDto {
    private Long userId;
    private String fuelGradeName;
    private Double sumVolume;
}
