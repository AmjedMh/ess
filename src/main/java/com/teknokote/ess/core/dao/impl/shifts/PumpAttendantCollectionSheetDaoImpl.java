package com.teknokote.ess.core.dao.impl.shifts;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.shifts.PumpAttendantCollectionSheetMapper;
import com.teknokote.ess.core.dao.shifts.PumpAttendantCollectionSheetDao;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.model.shifts.PaymentMethod;
import com.teknokote.ess.core.model.shifts.PumpAttendantCollectionSheet;
import com.teknokote.ess.core.model.shifts.PumpAttendantCollectionSheetDetail;
import com.teknokote.ess.core.model.shifts.ShiftPlanningExecution;
import com.teknokote.ess.core.repository.shifts.PumpAttendantCollectionSheetRepository;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Getter
@Setter
public class PumpAttendantCollectionSheetDaoImpl extends JpaGenericDao<Long, PumpAttendantCollectionSheetDto, PumpAttendantCollectionSheet> implements PumpAttendantCollectionSheetDao {
    @Autowired
    private PumpAttendantCollectionSheetMapper mapper;
    @Autowired
    private PumpAttendantCollectionSheetRepository repository;

    @Override
    protected PumpAttendantCollectionSheet beforeCreate(PumpAttendantCollectionSheet pumpAttendantCollectionSheet, PumpAttendantCollectionSheetDto dto) {
        pumpAttendantCollectionSheet.setPumpAttendant(getEntityManager().getReference(PumpAttendant.class, dto.getPumpAttendantId()));
        pumpAttendantCollectionSheet.setShiftPlanningExecution(getEntityManager().getReference(ShiftPlanningExecution.class, dto.getShiftPlanningExecutionId()));

        PumpAttendantCollectionSheet savedAttendantCollectionSheet = super.beforeCreate(pumpAttendantCollectionSheet, dto);
        savedAttendantCollectionSheet.getCollectionSheetDetails().forEach(pumpAttendantCollectionSheetDetail -> pumpAttendantCollectionSheetDetail.setPumpAttendantCollectionSheet(savedAttendantCollectionSheet));
        return savedAttendantCollectionSheet;
    }

    @Override
    protected PumpAttendantCollectionSheet beforeUpdate(PumpAttendantCollectionSheet pumpAttendantCollectionSheet, PumpAttendantCollectionSheetDto dto) {
        for (PumpAttendantCollectionSheetDetail detail : pumpAttendantCollectionSheet.getCollectionSheetDetails()) {
            detail.setPaymentMethod(getEntityManager().getReference(PaymentMethod.class, detail.getPaymentMethodId()));
            detail.setPumpAttendantCollectionSheet(pumpAttendantCollectionSheet);
        }

        return super.beforeUpdate(pumpAttendantCollectionSheet, dto);
    }

    @Override
    public List<PumpAttendantCollectionSheetDto> findByShiftPlanningExecutionId(Long shiftPlanningExecutionId) {
        return getRepository().findByShiftPlanningExecutionId(shiftPlanningExecutionId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }
}
