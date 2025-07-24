package com.teknokote.ess.core.service.impl;

import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.dao.FunctionDao;
import com.teknokote.ess.core.service.FunctionService;
import com.teknokote.ess.core.service.impl.validators.FunctionValidator;
import com.teknokote.ess.dto.FunctionDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
public class FunctionServiceImpl extends GenericCheckedService<Long, FunctionDto> implements FunctionService
{
   @Autowired
   private FunctionValidator validator;
   @Autowired
   private FunctionDao dao;

}
