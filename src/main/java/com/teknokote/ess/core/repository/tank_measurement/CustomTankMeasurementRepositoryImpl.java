package com.teknokote.ess.core.repository.tank_measurement;

import com.teknokote.ess.core.model.movements.TankMeasurement;
import com.teknokote.ess.dto.TankFilterDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public class CustomTankMeasurementRepositoryImpl implements CustomTankMeasurementRepository{
    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public Page<TankMeasurement> findMeasurementByFilterAndIdController(Long idCtr, TankFilterDto filterDto, int page, int size) {
        return FindTankMeasurementQueryBuilder.init(entityManager, PageRequest.of(page, size))
                .addControllerIdClause(idCtr)
                .addTankIdConfClause(filterDto.getTank())
                .addDateTimeStartBetweenClause(filterDto.getPeriod())
                .getResults();
    }
}
