package com.teknokote.ess.core.dao.mappers.shifts;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionalEntityDtoMapper;
import com.teknokote.ess.core.model.shifts.PaymentMethod;
import com.teknokote.ess.dto.shifts.PaymentMethodDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface PaymentMethodMapper extends BidirectionalEntityDtoMapper<Long, PaymentMethod, PaymentMethodDto>
{
}
