package com.teknokote.ess.core.dao.impl;

import com.teknokote.ess.core.dao.FunctionDao;
import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.FunctionMapper;
import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.core.model.organization.Function;
import com.teknokote.ess.core.repository.FunctionRepository;
import com.teknokote.ess.dto.FunctionDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Getter
public class FunctionDaoImpl extends JpaGenericDao<Long, FunctionDto, Function> implements FunctionDao
{
   @Autowired
   private FunctionMapper mapper;
   @Autowired
   private FunctionRepository repository;

   @Override
   public List<FunctionDto> findByFunctionalScope(EnumFunctionalScope scope)
   {
      return getRepository().findAllByScope(scope).stream().map(getMapper()::toDto).collect(Collectors.toList());
   }
}
