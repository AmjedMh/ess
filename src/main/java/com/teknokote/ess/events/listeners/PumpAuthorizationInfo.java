package com.teknokote.ess.events.listeners;

import com.teknokote.ess.events.publish.cm.EnumCardStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PumpAuthorizationInfo {
    private Long authorizationId;
    private Long cardId;
    private Long requestId;
    private Long transactionId;
    private EnumCardStatus status;

    public PumpAuthorizationInfo(Long authorizationId,Long cardId, Long requestId, Long transactionId,EnumCardStatus status) {
        this.authorizationId=authorizationId;
        this.cardId = cardId;
        this.requestId = requestId;
        this.transactionId = transactionId;
        this.status = status;
    }
}
