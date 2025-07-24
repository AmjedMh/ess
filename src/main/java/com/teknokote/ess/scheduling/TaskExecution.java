package com.teknokote.ess.scheduling;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class TaskExecution implements AutoCloseable
{
   private AtomicBoolean verrou;
   private TaskExecution(AtomicBoolean verrou){
      this.verrou=verrou;
   }

   public static TaskExecution of(AtomicBoolean verrou){
      return new TaskExecution(verrou);
   }
   @Override
   public void close()
   {
      this.verrou.set(false);
   }
}
