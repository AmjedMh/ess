package com.teknokote.ess.dto.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
public class DataPumpPrice {
    @JsonProperty("Pump")
    private Long pump;
    @JsonProperty("Price")
    private Long price;
}
