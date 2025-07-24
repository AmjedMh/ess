package com.teknokote.ess.core.repository.transactions;

import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.dto.TransactionFilterDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;


public class CustomTransactionRepositoryImpl implements CustomTransactionRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<PumpTransaction> findByCriteria(Long idCtr, TransactionFilterDto filterDto, int page, int size) {
        return FindTransactionsQueryBuilder.init(entityManager, PageRequest.of(page, size))
                .addControllerIdClause(idCtr)
                .addPumpIdConfClause(filterDto.getPumpIds())
                .addVolumeClause(filterDto.getVolume())
                .addFuelGradeIdConfClause(filterDto.getFuelGradeIds())
                .addDateTimeStartBetweenClause(filterDto.getPeriod())
                .addPumpAttendantClause(filterDto.getPumpAttendantIds())
                .getResults();
    }
}
