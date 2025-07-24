package com.teknokote.ess.core.service.shifts;

import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.shifts.PaymentMethodDto;

import java.util.List;

public interface PaymentMethodService extends BaseService<Long, PaymentMethodDto>
{
    List<PaymentMethodDto> findByStationId(Long stationId);

}
