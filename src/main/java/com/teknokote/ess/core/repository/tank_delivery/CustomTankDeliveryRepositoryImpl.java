package com.teknokote.ess.core.repository.tank_delivery;

import com.teknokote.ess.core.model.movements.TankDelivery;
import com.teknokote.ess.dto.TankFilterDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public class CustomTankDeliveryRepositoryImpl implements CustomTankDeliveryRepository {
    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public Page<TankDelivery> findTankDeliveryByIdController(Long idCtr, TankFilterDto filterDto, int page, int size) {
        return FindTankDeliveryQueryBuilder.init(entityManager, PageRequest.of(page, size))
                .addControllerIdClause(idCtr)
                .addTankIdConfClause(filterDto.getTank())
                .addDateTimeStartBetweenClause(filterDto.getPeriod())
                .getResults();
    }
}
