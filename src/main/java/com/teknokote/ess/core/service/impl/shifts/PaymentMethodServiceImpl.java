package com.teknokote.ess.core.service.impl.shifts;

import com.teknokote.ess.core.dao.shifts.PaymentMethodDao;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.service.shifts.PaymentMethodService;
import com.teknokote.ess.dto.shifts.PaymentMethodDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Getter
public class PaymentMethodServiceImpl extends GenericCheckedService<Long, PaymentMethodDto> implements PaymentMethodService
{
    @Autowired
    private ESSValidator<PaymentMethodDto> validator;
    @Autowired
    private PaymentMethodDao dao;

    @Override
    public List<PaymentMethodDto> findByStationId(Long stationId) {
        return getDao().findByStationId(stationId);
    }
}
