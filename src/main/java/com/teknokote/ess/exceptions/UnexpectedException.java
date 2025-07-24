package com.teknokote.ess.exceptions;

import lombok.Getter;

@Getter
public class UnexpectedException extends RuntimeException
{
   private final String message;
   private final Exception exception;

   public UnexpectedException(String message, Exception exception){
      this.message = message;
      this.exception = exception;
   }

}
