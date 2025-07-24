package com.teknokote.ess.dto.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
public class DataPumpAuthorize {
    @JsonProperty("Pump")
    private Long pump;
    @JsonProperty("Nozzle")
    private Long nozzle;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("Dose")
    private Long dose;
    @JsonProperty("Price")
    private Long price;
}
