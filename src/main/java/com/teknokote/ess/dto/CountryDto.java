package com.teknokote.ess.dto;

import com.teknokote.core.dto.ESSIdentifiedDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class CountryDto extends ESSIdentifiedDto<Long>
{
   @NotEmpty
   private String code;
   private String name;
   @NotNull
   private Long currencyId;
   private CurrencyDto currency;

   @Builder
   public CountryDto(Long id,Long version,String code, String name, Long currencyId,CurrencyDto currency)
   {
      super(id,version);
      this.code = code;
      this.name = name;
      this.currencyId = currencyId;
      this.currency=currency;
   }

   @Override
   public boolean equals(Object o)
   {
      if(this == o) return true;
      if(o == null || getClass() != o.getClass()) return false;

      CountryDto that = (CountryDto) o;

      return Objects.equals(code, that.code);
   }

   @Override
   public int hashCode()
   {
      return code != null ? code.hashCode() : 0;
   }
}
