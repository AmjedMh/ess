package com.teknokote.ess.core.repository.shifts;

import com.teknokote.ess.core.model.shifts.PumpAttendantCollectionSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PumpAttendantCollectionSheetRepository extends JpaRepository<PumpAttendantCollectionSheet, Long>
{
    List<PumpAttendantCollectionSheet> findByShiftPlanningExecutionId(Long shiftPlanningExecutionId);
}
