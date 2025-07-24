package com.teknokote.ess.dto.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataPumpTransaction {
    @JsonProperty("Pump")
    private Long pump;
    @JsonProperty("Transaction")
    private Long transaction;
}
