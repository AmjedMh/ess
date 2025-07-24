package com.teknokote.ess.core.service;

import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.UserScopeDto;

import java.util.List;

public interface UserScopeService extends BaseService<Long, UserScopeDto>
{
   List<UserScopeDto> findByFunctionalScopeAndUser(EnumFunctionalScope scope, Long userId);
}
