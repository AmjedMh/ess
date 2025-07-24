package com.teknokote.ess.core.model.shifts;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

/**
 * Fiche de recette d'un pompiste pour un ShiftPlanningExecution
 * Dans cette fiche, pour un pompiste, on note:
 * - Moyen de paiement
 * - montant
 *
 * A savoir que pour la journée en question, la somme totale des sheets doit être
 * égale au montant des ventes.
 */
@Entity
@Getter
@Setter
public class PumpAttendantCollectionSheet extends ESSEntity<Long, User>
{
   @Serial
   private static final long serialVersionUID = -3897038936143726165L;
   @ManyToOne
   private ShiftPlanningExecution shiftPlanningExecution;
   @Column(name = "shift_planning_execution_id",insertable = false, updatable = false)
   private Long shiftPlanningExecutionId;
   @ManyToOne
   private PumpAttendant pumpAttendant;
   @Column(name = "pump_attendant_id",insertable = false, updatable = false)
   private Long pumpAttendantId;
   @Enumerated(EnumType.STRING)
   private EnumPumpAttendantSheetStatus status;
   private String inconsistencyMotif;
   // Montant global de la recette du pompiste sur le shift
   private BigDecimal totalAmount;
   @OneToMany(mappedBy = "pumpAttendantCollectionSheet",cascade = CascadeType.ALL, orphanRemoval = true)
   private Set<PumpAttendantCollectionSheetDetail> collectionSheetDetails;

   @Override
   public boolean equals(Object o)
   {
      if(this == o) return true;
      if(o == null || getClass() != o.getClass()) return false;

      PumpAttendantCollectionSheet that = (PumpAttendantCollectionSheet) o;

      if(!Objects.equals(shiftPlanningExecutionId, that.shiftPlanningExecutionId))
         return false;
      return Objects.equals(pumpAttendantId, that.pumpAttendantId);
   }

   @Override
   public int hashCode()
   {
      int result = shiftPlanningExecutionId != null ? shiftPlanningExecutionId.hashCode() : 0;
      result = 31 * result + (pumpAttendantId != null ? pumpAttendantId.hashCode() : 0);
      return result;
   }
}
