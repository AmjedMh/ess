package com.teknokote.ess.core.repository.tank_delivery;

import com.teknokote.ess.core.model.movements.TankDelivery;
import com.teknokote.ess.dto.TankFilterDto;
import org.springframework.data.domain.Page;

public interface CustomTankDeliveryRepository {
    Page<TankDelivery> findTankDeliveryByIdController(Long idCtr, TankFilterDto filterDto, int page, int size);

}
