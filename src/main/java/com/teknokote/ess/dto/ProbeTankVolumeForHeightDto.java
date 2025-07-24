package com.teknokote.ess.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProbeTankVolumeForHeightDto {
    private Long probe;
    private float height;
    private float volume;
}
