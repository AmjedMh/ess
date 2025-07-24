package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency,Long>
{

}
