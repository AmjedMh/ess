package com.teknokote.ess.core.dao;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.dto.ShiftDto;

import java.util.List;

public interface ShiftDao extends BasicDao<Long, ShiftDto>
{
   List<ShiftDto> findOffDay();
}

