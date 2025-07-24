package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
