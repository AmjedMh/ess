package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.Payment;
import com.teknokote.ess.core.repository.PaymentRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl extends AbstractEntityService<Payment, Long> implements PaymentService{

    public PaymentServiceImpl(PaymentRepository dao) {
        super(dao);
    }

}
