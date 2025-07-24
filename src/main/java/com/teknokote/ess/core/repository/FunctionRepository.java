package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.core.model.organization.Function;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FunctionRepository extends JpaRepository<Function, Long>
{
   List<Function> findAllByScope(EnumFunctionalScope scope);
}
