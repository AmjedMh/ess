package com.teknokote.ess.dto.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataTank {
    @JsonProperty("Tank")
    private Long tank;
    @JsonProperty("DateTimeStart")
    private String dateTimeStart;
    @JsonProperty("DateTimeEnd")
    private String dateTimeEnd;
}
