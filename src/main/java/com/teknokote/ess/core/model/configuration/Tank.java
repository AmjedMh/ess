package com.teknokote.ess.core.model.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.ess.core.model.common.ControllerIdentifiedESSEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Tank extends ControllerIdentifiedESSEntity
{
   private static final long serialVersionUID = -6193769135468357227L;

   private Long height;
   @Column(nullable = false)
   private Long criticalHighProductAlarm;
   @Column(nullable = false)
   private Long highProductAlarm;
   private Long lowProductAlarmHeight;
   @Column(nullable = false)
   private Long criticalLowProductAlarm;
   @Column(nullable = false)
   private Long highWaterAlarmHeight;
   @Column(nullable = false)
   private Boolean setStopPumpsAtCriticalLowProductHeight;
   @ManyToOne
   @JsonIgnore
   public FuelGrade grade;
   @ManyToOne
   @JsonIgnore
   public ControllerPtsConfiguration controllerPtsConfiguration;
}
