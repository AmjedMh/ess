package com.teknokote.ess.core.repository.shifts;

import com.teknokote.ess.core.model.shifts.AffectedPumpAttendant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AffectedPumpAttendantRepository extends JpaRepository<AffectedPumpAttendant, Long>
{
}
