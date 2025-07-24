package com.teknokote.ess.core.repository.shifts;

import com.teknokote.ess.core.model.shifts.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long>
{
    @Query("SELECT pm FROM PaymentMethod pm " +
            "JOIN pm.customerAccount ca " +
            "JOIN ca.stations st " +
            "WHERE st.id = :stationId")
    List<PaymentMethod> findByStationId(Long stationId);
}
