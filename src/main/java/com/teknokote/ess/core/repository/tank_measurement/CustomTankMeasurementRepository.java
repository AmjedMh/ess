package com.teknokote.ess.core.repository.tank_measurement;

import com.teknokote.ess.core.model.movements.TankMeasurement;
import com.teknokote.ess.dto.TankFilterDto;
import org.springframework.data.domain.Page;

public interface CustomTankMeasurementRepository {
    Page<TankMeasurement> findMeasurementByFilterAndIdController(Long idCtr, TankFilterDto filterDto, int page, int size);
}
