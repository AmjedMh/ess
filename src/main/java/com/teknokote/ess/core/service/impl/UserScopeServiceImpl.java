package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.UserScopeDao;
import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.core.service.UserScopeService;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.dto.UserScopeDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Getter
public class UserScopeServiceImpl extends GenericCheckedService<Long, UserScopeDto> implements UserScopeService
{
   @Autowired
   private ESSValidator<UserScopeDto> validator;
   @Autowired
   private UserScopeDao dao;

   @Override
   public List<UserScopeDto> findByFunctionalScopeAndUser(EnumFunctionalScope scope, Long userId)
   {
      return getDao().findByFunctionalScopeAndUser(scope, userId);
   }
}
