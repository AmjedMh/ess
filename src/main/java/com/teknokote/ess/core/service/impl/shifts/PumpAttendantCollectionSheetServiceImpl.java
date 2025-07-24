package com.teknokote.ess.core.service.impl.shifts;

import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.dao.shifts.PumpAttendantCollectionSheetDao;
import com.teknokote.ess.core.service.shifts.PumpAttendantCollectionSheetService;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
public class PumpAttendantCollectionSheetServiceImpl extends GenericCheckedService<Long, PumpAttendantCollectionSheetDto> implements PumpAttendantCollectionSheetService {
    @Autowired
    private ESSValidator<PumpAttendantCollectionSheetDto> validator;
    @Autowired
    private PumpAttendantCollectionSheetDao dao;
}
