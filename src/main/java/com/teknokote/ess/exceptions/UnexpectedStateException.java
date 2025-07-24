package com.teknokote.ess.exceptions;

import lombok.Getter;

public class UnexpectedStateException extends RuntimeException
{
   @Getter
   private final String message;
   public UnexpectedStateException(String message)
   {
      this.message = message;
   }
}
