package com.teknokote.ess.dto.shifts;

import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentMethodDto extends ESSIdentifiedDto<Long>{
    private String code;
    private String description;
    private Long customerAccountId;
    @Builder
    public PaymentMethodDto(Long id,Long version,String code,String description,Long customerAccountId)
    {
        super(id,version);
        this.code = code;
        this.description = description;
        this.customerAccountId = customerAccountId;
    }
}
