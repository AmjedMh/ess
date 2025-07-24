package com.teknokote.ess.dto.requests;

import com.teknokote.ess.core.model.requests.EnumRequestStatus;
import com.teknokote.ess.core.model.requests.EnumRequestType;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AbstractRequestDto extends ESSIdentifiedDto<Long>
{
   private Long stationId;
   private EnumRequestType requestType;
   private LocalDateTime scheduledDate;
   private LocalDateTime createdDate;
   private LocalDateTime plannedDate;
   private LocalDateTime executionDate;
   private LocalDateTime lastTrialDate;
   private Long requesterId;
   private UserDto requester;
   private EnumRequestStatus status;

   public AbstractRequestDto(Long id, Long version,Long stationId,EnumRequestType requestType,LocalDateTime scheduledDate,LocalDateTime createdDate,
                             LocalDateTime plannedDate,LocalDateTime executionDate,LocalDateTime lastTrialDate,Long requesterId,UserDto requester,EnumRequestStatus status)
   {
      super(id, version);
      this.stationId=stationId;
      this.requestType=requestType;
      this.scheduledDate=scheduledDate;
      this.createdDate=createdDate;
      this.plannedDate=plannedDate;
      this.executionDate=executionDate;
      this.lastTrialDate=lastTrialDate;
      this.requesterId=requesterId;
      this.requester=requester;
      this.status=status;
   }
}
