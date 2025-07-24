package com.teknokote.ess.core.model.organization;

import com.teknokote.core.model.ESSEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Function extends ESSEntity<Long,User>
{
   private static final long serialVersionUID = 8_224_234_998_640_362_111L;

   public enum EnumFunction {
      MANAGE_USERS(EnumFunctionalScope.GLOBAL),
      MANAGE_CUSTOMER_ACCOUNTS(EnumFunctionalScope.GLOBAL),
      MANAGE_ATTACHED_USERS(EnumFunctionalScope.CUSTOMER_ACCOUNT),
      MANAGE_STATIONS(EnumFunctionalScope.CUSTOMER_ACCOUNT),
      SALES(EnumFunctionalScope.STATION), //
      PURCHASES(EnumFunctionalScope.STATION), //
      TAG_ACTIVITIES(EnumFunctionalScope.STATION), //
      TRANSACTIONS(EnumFunctionalScope.STATION); //

      @Getter
      private EnumFunctionalScope functionalScope;
      EnumFunction(EnumFunctionalScope functionalScope){
         this.functionalScope = functionalScope;
      }
   }

   /**
    * Précise le niveau du périmètre auquel la fonction est attachée (définit).
    */
   @Enumerated(EnumType.STRING)
   private EnumFunctionalScope scope;

   @Column(nullable = false)
   @Enumerated(EnumType.STRING)
   private EnumFunction code;
   private String description;

   @Override
   public boolean equals(Object o)
   {
      if(this == o) return true;
      if(o == null || getClass() != o.getClass()) return false;

      Function function = (Function) o;

      return code.equals(function.code);
   }

   @Override
   public int hashCode()
   {
      return code.hashCode();
   }
}
