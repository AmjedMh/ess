package com.teknokote.ess.core.service.impl.shifts;

import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.dao.shifts.AffectedPumpAttendantDao;
import com.teknokote.ess.core.service.shifts.AffectedPumpAttendantService;
import com.teknokote.ess.dto.shifts.AffectedPumpAttendantDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
public class AffectedPumpAttendantServiceImpl extends GenericCheckedService<Long, AffectedPumpAttendantDto> implements AffectedPumpAttendantService {
    @Autowired
    private ESSValidator<AffectedPumpAttendantDto> validator;
    @Autowired
    private AffectedPumpAttendantDao dao;
}
