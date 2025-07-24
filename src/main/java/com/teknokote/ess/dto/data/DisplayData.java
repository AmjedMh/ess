package com.teknokote.ess.dto.data;

import lombok.Data;

@Data
public class DisplayData {

    private Long pump;
    private Long lastNozzle;
    private Long lastTransaction;
    private float volume;
    private float amount;
}
