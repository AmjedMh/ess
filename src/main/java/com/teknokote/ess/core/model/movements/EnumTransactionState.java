package com.teknokote.ess.core.model.movements;

import lombok.Getter;

import java.util.Arrays;

public enum EnumTransactionState
{
   WAITING_NOZZLE_UP_FOR_AUTHORIZATION("WaitingNozzleUpForAuthorization"),
   AUTHORIZED("Authorized"),
   END_OF_TRANSACTION("EndOfTransaction"),
   NOT_FOUND("Not found"),
   FINISHED("Finished"),
   FILLING("Filling");

   @Getter
   private String label;

   EnumTransactionState(String aLabel){
      this.label = aLabel;
   }

   public static EnumTransactionState getStateByLabel(String aLabel){
      return Arrays.stream(EnumTransactionState.values())
         .filter(el->el.getLabel().equals(aLabel))
         .findFirst()
         .orElseThrow(()->new RuntimeException("Pas de correspondance à l'état "+ aLabel));
   }
}
