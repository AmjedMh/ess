package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.PumpPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PumpPricesRepository extends JpaRepository<PumpPrice,Long> {

}
