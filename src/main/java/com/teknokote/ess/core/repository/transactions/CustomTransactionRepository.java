package com.teknokote.ess.core.repository.transactions;

import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.dto.TransactionFilterDto;
import org.springframework.data.domain.Page;

public interface CustomTransactionRepository {
    Page<PumpTransaction> findByCriteria(Long idCtr, TransactionFilterDto filterDto, int page, int size);
}
