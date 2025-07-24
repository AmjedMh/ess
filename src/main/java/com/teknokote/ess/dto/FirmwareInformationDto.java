package com.teknokote.ess.dto;

import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FirmwareInformationDto extends ESSIdentifiedDto<Long>
{
   private String ptsId;
   private String dateTime;
   private boolean versionState;
   private LocalDateTime modificationDate;

   @Builder
   public FirmwareInformationDto(Long id,Long version,String ptsId,String dateTime,boolean versionState,LocalDateTime modificationDate)
   {
      super(id,version);
      this.ptsId=ptsId;
      this.dateTime=dateTime;
      this.versionState=versionState;
      this.modificationDate=modificationDate;
   }
}
