package com.teknokote.ess.dto;

import com.teknokote.core.dto.ESSIdentifiedDto;
import com.teknokote.ess.core.model.organization.EnumControllerType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ControllerPtsDto extends ESSIdentifiedDto<Long>
{
   @NotEmpty
   private String ptsId;
   // juste pour toDto()
   private FirmwareInformationDto currentFirmwareInformation;
   private Long currentFirmwareInformationId;
   private Long currentConfigurationId;
   private UserDto userController;
   private EnumControllerType controllerType;
   private String phone;
   @Builder
   public ControllerPtsDto(Long id,Long version, String ptsId,  Long currentFirmwareInformationId,Long currentConfigurationId,String phone,EnumControllerType controllerType,UserDto userController,FirmwareInformationDto currentFirmwareInformation)
   {
      super(id,version);
      this.ptsId = ptsId;
      this.currentFirmwareInformationId = currentFirmwareInformationId;
      this.currentConfigurationId=currentConfigurationId;
      this.currentFirmwareInformation=currentFirmwareInformation;
      this.userController=userController;
      this.controllerType=controllerType;
      this.phone=phone;
   }
}
