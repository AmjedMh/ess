package com.teknokote.ess.core.dao.impl.shifts;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.shifts.PumpAttendantCollectionSheetDetailMapper;
import com.teknokote.ess.core.dao.shifts.PumpAttendantCollectionSheetDetailDao;
import com.teknokote.ess.core.model.shifts.PaymentMethod;
import com.teknokote.ess.core.model.shifts.PumpAttendantCollectionSheet;
import com.teknokote.ess.core.model.shifts.PumpAttendantCollectionSheetDetail;
import com.teknokote.ess.core.repository.shifts.PumpAttendantCollectionSheetDetailRepository;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDetailDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@Getter
@Setter
public class PumpAttendantCollectionSheetDetailDaoImpl extends JpaGenericDao<Long, PumpAttendantCollectionSheetDetailDto, PumpAttendantCollectionSheetDetail> implements PumpAttendantCollectionSheetDetailDao
{
    @Autowired
    private PumpAttendantCollectionSheetDetailMapper mapper;
    @Autowired
    private PumpAttendantCollectionSheetDetailRepository repository;
    @Override
    protected PumpAttendantCollectionSheetDetail beforeCreate(PumpAttendantCollectionSheetDetail pumpAttendantCollectionSheetDetail, PumpAttendantCollectionSheetDetailDto dto) {
        pumpAttendantCollectionSheetDetail.setPaymentMethod(getEntityManager().getReference(PaymentMethod.class,dto.getPaymentMethodId()));
        pumpAttendantCollectionSheetDetail.setPumpAttendantCollectionSheet(getEntityManager().getReference(PumpAttendantCollectionSheet.class,dto.getPumpAttendantCollectionSheetId()));
        return super.beforeCreate(pumpAttendantCollectionSheetDetail,dto);
    }
}
