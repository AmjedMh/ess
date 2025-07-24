package com.teknokote.ess.dto;

import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.core.dto.ESSIdentifiedDto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserScopeDto extends ESSIdentifiedDto<Long>
{
   @NotNull
   private EnumFunctionalScope scope;
   @NotNull
   private UserDto relatedUser;
   private Long customerAccountId;
   private Long stationId;
   private Long pumpId;
   private Set<FunctionDto> scopeFunctions;

   @Builder
   public UserScopeDto(Long id,Long version,EnumFunctionalScope scope,UserDto relatedUser,Long customerAccountId,Long stationId,Long pumpId,Set<FunctionDto> scopeFunctions)
   {
      super(id,version);
      this.scope=scope;
      this.relatedUser=relatedUser;
      this.customerAccountId=customerAccountId;
      this.stationId=stationId;
      this.pumpId=pumpId;
      this.scopeFunctions=scopeFunctions;
   }
}
