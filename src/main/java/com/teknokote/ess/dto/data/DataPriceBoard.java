package com.teknokote.ess.dto.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
public class DataPriceBoard {
    @JsonProperty("PriceBoard")
    private Long priceBoard;
}
