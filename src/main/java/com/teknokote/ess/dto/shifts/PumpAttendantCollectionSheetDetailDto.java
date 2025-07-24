package com.teknokote.ess.dto.shifts;

import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PumpAttendantCollectionSheetDetailDto extends ESSIdentifiedDto<Long>{
    private Long pumpAttendantCollectionSheetId;
    private PaymentMethodDto paymentMethod;
    private Long paymentMethodId;
    private BigDecimal amount;
    @Builder
    public PumpAttendantCollectionSheetDetailDto(Long id,Long version,Long pumpAttendantCollectionSheetId,PaymentMethodDto paymentMethod,Long paymentMethodId,BigDecimal amount)
    {
        super(id,version);
        this.pumpAttendantCollectionSheetId = pumpAttendantCollectionSheetId;
        this.paymentMethod = paymentMethod;
        this.paymentMethodId = paymentMethodId;
        this.amount = amount;
    }
}
