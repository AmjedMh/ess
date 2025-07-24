package com.teknokote.ess.events.publish.cm;

import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
public class AuthorizationDto extends ESSIdentifiedDto<Long> {
    EnumAuthorizationStatus status;
    private String customerAccountReference;
    private String reference;
    private LocalDateTime dateTime;
    private BigDecimal amount;
    private BigDecimal quantity;
    private Long cardId;
    private EnumCardType cardType;
    String ptsId;
    Long pump;
    @Builder
    public AuthorizationDto(Long id, Long version,EnumAuthorizationStatus status,String reference,LocalDateTime dateTime,BigDecimal amount,BigDecimal quantity,Long cardId,EnumCardType cardType,String ptsId,Long pump,String customerAccountReference) {
        super(id, version);
        this.customerAccountReference=customerAccountReference;
        this.status=status;
        this.reference=reference;
        this.dateTime=dateTime;
        this.amount=amount;
        this.quantity=quantity;
        this.cardId=cardId;
        this.cardType=cardType;
        this.ptsId=ptsId;
        this.pump=pump;
    }
}
