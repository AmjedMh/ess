package com.teknokote.ess.events.publish;

import com.teknokote.ess.events.publish.cm.AuthorizationDto;
import com.teknokote.ess.events.publish.cm.AuthorizationRequest;
import com.teknokote.ess.events.publish.cm.CMTransactionDto;
import com.teknokote.ess.events.publish.cm.EnumCardStatus;
import org.springframework.stereotype.Component;

@Component
public interface CustomerAccountAuthorization {
    AuthorizationDto requestAuthorization(AuthorizationRequest authorizationRequest);
    CMTransactionDto saveTransaction(CMTransactionDto cmTransactionDto);

    AuthorizationDto getAuthorization(String ptsId, Long pump,String tag);
    void updateCardStatus(Long cardId,Long authorizationId,Long transactionId, EnumCardStatus status);
}
