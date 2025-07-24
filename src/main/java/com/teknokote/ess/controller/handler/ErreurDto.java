package com.teknokote.ess.controller.handler;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class ErreurDto
{
   private LocalDateTime timestamp;
   private int status;
   private String codeError;
   private String message;
}
