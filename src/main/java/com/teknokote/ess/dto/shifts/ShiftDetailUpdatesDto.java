package com.teknokote.ess.dto.shifts;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
public class ShiftDetailUpdatesDto
{
   @NotNull
   private Long id;
   @NotNull
   private Long stationId;
   @NotNull
   private Long shiftPlanningExecutionId;
   @NotNull
   private Long pumpAttendantId;
   @NotNull
   private LocalDateTime startDateTime;
   @NotNull
   private LocalDateTime endDateTime;
   private BigDecimal tankReturn;
   private Boolean reinforcementRequested; // Pour les pompistes Renfort

   @Builder
   public ShiftDetailUpdatesDto(Long shiftPlanningExecutionId, Long id, Long pumpAttendantId, LocalDateTime startDateTime, LocalDateTime endDateTime, BigDecimal tankReturn, Boolean reinforcementRequested)
   {
      this.shiftPlanningExecutionId=shiftPlanningExecutionId;
      this.id = id;
      this.pumpAttendantId=pumpAttendantId;
      this.startDateTime=startDateTime;
      this.endDateTime=endDateTime;
      this.tankReturn=tankReturn;
      this.reinforcementRequested = reinforcementRequested;
   }

   public boolean isReinforcementRequested(){
      return Objects.nonNull(reinforcementRequested) && reinforcementRequested;
   }
}
