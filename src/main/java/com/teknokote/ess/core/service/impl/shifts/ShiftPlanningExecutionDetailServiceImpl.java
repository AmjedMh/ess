package com.teknokote.ess.core.service.impl.shifts;

import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningExecutionDetailDao;
import com.teknokote.ess.core.service.shifts.ShiftPlanningExecutionDetailService;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDetailDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
public class ShiftPlanningExecutionDetailServiceImpl extends GenericCheckedService<Long, ShiftPlanningExecutionDetailDto> implements ShiftPlanningExecutionDetailService {
    @Autowired
    private ESSValidator<ShiftPlanningExecutionDetailDto> validator;
    @Autowired
    private ShiftPlanningExecutionDetailDao dao;

}
