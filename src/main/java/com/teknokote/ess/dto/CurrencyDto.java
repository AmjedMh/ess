package com.teknokote.ess.dto;

import com.teknokote.core.dto.ESSIdentifiedDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrencyDto extends ESSIdentifiedDto<Long>
{
   @NotEmpty
   private String code;
   private String name;
   private String locale;

   @Builder
   public CurrencyDto(Long id,Long version, String code, String name, String locale)
   {
      super(id,version);
      this.code = code;
      this.name = name;
      this.locale = locale;
   }
}
