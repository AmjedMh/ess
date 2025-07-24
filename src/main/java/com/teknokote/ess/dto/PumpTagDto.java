package com.teknokote.ess.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PumpTagDto {
    private Long pump;
    private Long nozzle;
    private String tag;
}
