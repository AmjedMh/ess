package com.teknokote.ess.core.dao;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.dto.UserScopeDto;

import java.util.List;


public interface UserScopeDao extends BasicDao<Long, UserScopeDto>
{
   List<UserScopeDto> findByFunctionalScopeAndUser(EnumFunctionalScope scope, Long userId);
}
