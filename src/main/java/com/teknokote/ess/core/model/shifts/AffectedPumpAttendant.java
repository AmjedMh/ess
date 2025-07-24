package com.teknokote.ess.core.model.shifts;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.model.organization.PumpAttendantTeam;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Entity
@Getter
@Setter
public class AffectedPumpAttendant extends ESSEntity<Long, User>
{
   @Serial
   private static final long serialVersionUID = 4254881468163223319L;
   @ManyToOne(fetch = FetchType.LAZY)
   private PumpAttendantTeam pumpAttendantTeam;
   @Column(name = "pump_attendant_team_id",insertable = false, updatable = false)
   private Long pumpAttendantTeamId;
   @ManyToOne(fetch = FetchType.LAZY)
   private Pump pump;
   @Column(name = "pump_id",insertable = false, updatable = false)
   private Long pumpId;
   @ManyToOne
   private PumpAttendant pumpAttendant;
   @Column(name = "pump_attendant_id",insertable = false, updatable = false)
   private Long pumpAttendantId;

}
