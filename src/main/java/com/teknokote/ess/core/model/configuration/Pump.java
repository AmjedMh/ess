package com.teknokote.ess.core.model.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.ess.core.model.common.ControllerIdentifiedESSEntity;
import com.teknokote.ess.core.model.movements.PumpTransaction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Pump extends ControllerIdentifiedESSEntity
{
   private static final long serialVersionUID = 970355920083648047L;

   private Long address;
   @OneToMany(fetch = FetchType.LAZY, mappedBy = "pump", cascade = CascadeType.ALL)
   private List<Nozzle> nozzleList;
   @OneToMany(fetch = FetchType.LAZY, mappedBy = "pump")
   private List<PumpTransaction> pumpTransactions;
   @ManyToOne
   @JsonIgnore
   public PumpPort port;
   @ManyToOne
   @JsonIgnore
   public ControllerPtsConfiguration controllerPtsConfiguration;

   @Override
   public boolean equals(Object o)
   {
      if(this == o) return true;
      if(o == null || getClass() != o.getClass()) return false;

      Pump pump = (Pump) o;

      if(!idConf.equals(pump.idConf)) return false;
      return controllerPtsConfiguration.equals(pump.controllerPtsConfiguration);
   }

   @Override
   public int hashCode()
   {
      int result = idConf.hashCode();
      result = 31 * result + controllerPtsConfiguration.hashCode();
      return result;
   }
}
