package com.teknokote.ess.core.dao.impl;

import com.teknokote.ess.core.dao.UserScopeDao;
import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.UserScopeMapper;
import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.model.organization.UserScope;
import com.teknokote.ess.core.repository.UserScopeRepository;
import com.teknokote.ess.dto.UserScopeDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Getter
@Setter
public class UserScopeDaoImpl extends JpaGenericDao<Long,UserScopeDto, UserScope> implements UserScopeDao
{
   @Autowired
   private UserScopeMapper mapper;
   @Autowired
   private UserScopeRepository repository;

   @Override
   public List<UserScopeDto> findByFunctionalScopeAndUser(EnumFunctionalScope scope, Long userId)
   {
      return getRepository().findAllByScopeAndRelatedUserId(scope,userId).stream().map(getMapper()::toDto).collect(Collectors.toList());
   }
   @Override
   protected UserScope beforeCreate(UserScope userScope, UserScopeDto dto) {
      userScope.setRelatedUser(getEntityManager().getReference(User.class, dto.getRelatedUser().getId()));
      return super.beforeCreate(userScope, dto);
   }
}
