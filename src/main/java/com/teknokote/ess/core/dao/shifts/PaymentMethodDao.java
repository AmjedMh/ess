package com.teknokote.ess.core.dao.shifts;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.dto.shifts.PaymentMethodDto;

import java.util.List;

public interface PaymentMethodDao extends BasicDao<Long, PaymentMethodDto>
{
    List<PaymentMethodDto> findByStationId(Long stationId);
}

