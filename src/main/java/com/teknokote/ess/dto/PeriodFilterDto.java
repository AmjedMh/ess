package com.teknokote.ess.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PeriodFilterDto
{
   private LocalDateTime from;
   private LocalDateTime to;

   public static PeriodFilterDto of(LocalDateTime start, LocalDateTime end)
   {
      return new PeriodFilterDto(start,end);
   }
}
