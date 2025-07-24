package com.teknokote.ess.core.service.impl.shifts;

import com.teknokote.ess.core.dao.shifts.PumpAttendantCollectionSheetDetailDao;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.service.shifts.PumpAttendantCollectionSheetDetailService;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDetailDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
public class PumpAttendantCollectionSheetDetailServiceImpl extends GenericCheckedService<Long, PumpAttendantCollectionSheetDetailDto> implements PumpAttendantCollectionSheetDetailService
{
    @Autowired
    private ESSValidator<PumpAttendantCollectionSheetDetailDto> validator;
    @Autowired
    private PumpAttendantCollectionSheetDetailDao dao;
}
