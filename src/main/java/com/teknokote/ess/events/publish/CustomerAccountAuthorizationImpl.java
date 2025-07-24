package com.teknokote.ess.events.publish;

import com.teknokote.ess.events.publish.cm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerAccountAuthorizationImpl implements  CustomerAccountAuthorization {
    @Autowired
    private CardManagerClient cardManagerClient;

    @Override
    public AuthorizationDto requestAuthorization(AuthorizationRequest authorizationRequest) {
       return cardManagerClient.authorize(authorizationRequest);
    }

    @Override
    public CMTransactionDto saveTransaction(CMTransactionDto cmTransactionDto) {
        return cardManagerClient.saveTransaction(cmTransactionDto);
    }

    @Override
    public void updateCardStatus(Long cardId,Long authorizationId,Long transactionId, EnumCardStatus status) {
        cardManagerClient.updateCardStatus(cardId,authorizationId,transactionId,status);
    }

    @Override
    public AuthorizationDto getAuthorization(String ptsId, Long pump , String tag) {
        return cardManagerClient.getAuthorization(ptsId,pump, tag);
    }
}
