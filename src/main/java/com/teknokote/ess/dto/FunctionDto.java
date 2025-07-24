package com.teknokote.ess.dto;

import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.core.model.organization.Function;
import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FunctionDto extends ESSIdentifiedDto<Long>
{
   private EnumFunctionalScope scope;
   private Function.EnumFunction code;
   private String description;

   @Builder
   public FunctionDto(Long id,Long version, EnumFunctionalScope scope, Function.EnumFunction code,String description)
   {
      super(id,version);
      this.scope=scope;
      this.code=code;
      this.description=description;
   }
}
