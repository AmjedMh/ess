package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.core.model.organization.UserScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserScopeRepository extends JpaRepository<UserScope, Long>
{
   List<UserScope> findAllByScopeAndRelatedUserId(EnumFunctionalScope scope,Long userId);
}
