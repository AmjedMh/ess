package com.teknokote.ess.core.repository.shifts;

import com.teknokote.ess.core.model.shifts.PumpAttendantCollectionSheetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PumpAttendantCollectionSheetDetailRepository extends JpaRepository<PumpAttendantCollectionSheetDetail, Long>
{
}
