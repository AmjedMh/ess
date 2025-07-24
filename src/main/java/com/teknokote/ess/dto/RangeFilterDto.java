package com.teknokote.ess.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RangeFilterDto
{
   Long min;
   Long max;
}
