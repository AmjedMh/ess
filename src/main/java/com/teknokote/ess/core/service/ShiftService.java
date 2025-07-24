package com.teknokote.ess.core.service;

import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.ShiftDto;

public interface ShiftService extends BaseService<Long, ShiftDto>
{

   ShiftDto findOffDay();
}
