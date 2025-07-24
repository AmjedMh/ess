package com.teknokote.ess.core.model.organization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.configuration.Pump;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.util.Objects;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserScope extends ESSEntity<Long,User>
{

   @Serial
   private static final long serialVersionUID = 5525823076431719806L;

   /**
    * Indique le niveau.
    * Si scope:
    * - CUSTOMER_ACCOUNT = juste le compte client est renseigné. Et la liste des fonctions est celle du niveau compte
    * - STATION = le compte client et la station doitevent être renseignés. Et la liste des fonctions est celle du niveau station
    */
   @Enumerated(EnumType.STRING)
   private EnumFunctionalScope scope;

   /**
    * Utilisateur propriétaire de ce périmètre de droit
    */
   @ManyToOne
   @JsonIgnore
   private User relatedUser;
   @Column(name = "related_user_id",insertable = false,updatable = false)
   private Long relatedUserId;
   /**
    * Compte client
    */
   @ManyToOne
   private CustomerAccount customerAccount;
   @Column(name = "customer_account_id",insertable = false,updatable = false)
   private Long customerAccountId;
   /**
    * Station du compte client concernée
    */
   @ManyToOne
   private Station station;
   @Column(name="station_id",insertable = false,updatable = false)
   private Long stationId;
   /**
    * Pompe de la station en question
    */
   @ManyToOne
   private Pump pump;
   @Column(name = "pump_id",insertable = false,updatable = false)
   private Long pumpId;
   /**
    * Ensemble des fonctions allouées sur le niveau
    */
   @ManyToMany(cascade = CascadeType.ALL)
   @JoinTable(name = "user_scope_functions",joinColumns = @JoinColumn(name = "user_scope_id"), inverseJoinColumns = @JoinColumn(name = "function_id"))
   private Set<Function> scopeFunctions;

   @Override
   public boolean equals(Object o)
   {
      if(this == o) return true;
      if(o == null || getClass() != o.getClass()) return false;

      UserScope userScope1 = (UserScope) o;

      if(scope != userScope1.scope) return false;
      if(!relatedUser.equals(userScope1.relatedUser)) return false;
      if(!Objects.equals(customerAccount, userScope1.customerAccount))
         return false;
      if(!Objects.equals(station, userScope1.station)) return false;
      return Objects.equals(pump, userScope1.pump);
   }

   @Override
   public int hashCode()
   {
      int result = scope.hashCode();
      result = 31 * result + relatedUser.hashCode();
      result = 31 * result + (customerAccount != null ? customerAccount.hashCode() : 0);
      result = 31 * result + (station != null ? station.hashCode() : 0);
      result = 31 * result + (pump != null ? pump.hashCode() : 0);
      return result;
   }
}
