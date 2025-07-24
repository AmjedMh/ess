package com.teknokote.ess.core.dao;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.dto.FunctionDto;

import java.util.List;

public interface FunctionDao extends BasicDao<Long, FunctionDto>
{
   List<FunctionDto> findByFunctionalScope(EnumFunctionalScope scope);

}
