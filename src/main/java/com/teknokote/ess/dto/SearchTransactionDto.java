package com.teknokote.ess.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SearchTransactionDto
{
   private String ptsId;
   private Long pumpId;
   private LocalDateTime startDate;
   private LocalDateTime endDate;
   private UploadType uploadType;
   public enum UploadType {
      TRANSACTIONS,
      TANK_MEASUREMENTS,
      ALL
   }

   @Override
   public String toString()
   {
      return "SearchTransactionDto{" +
         "ptsId='" + ptsId + '\'' +
         ", pumpId=" + pumpId +
         ", startDate=" + startDate +
         ", endDate=" + endDate +
         '}';
   }
}
