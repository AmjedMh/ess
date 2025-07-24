package com.teknokote.ess.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StationAndControllerDto {
    private String name;
    private String address;
    private String controllerPtsId;
    private Long countryId;
}
