package com.teknokote.ess.exceptions;

import java.util.Objects;

public class KeycloakUserCreationException extends RuntimeException
{
   private String reason;
   private Exception exception;
   public KeycloakUserCreationException(String reason){
      this.reason=reason;
   }
   public KeycloakUserCreationException(Exception exception){
      this.exception=exception;
   }

   public String getReason(){
      if(Objects.nonNull(exception)){
         return exception.getMessage();
      }
      return reason;
   }
}
