package com.teknokote.ess.dto.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
public class DataPumpNozzle {
    @JsonProperty("Pump")
    private Long pump;
    @JsonProperty("Nozzle")
    private Long nozzle;
}
