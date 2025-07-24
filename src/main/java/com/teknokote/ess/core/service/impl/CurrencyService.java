package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.CurrencyDao;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.service.impl.validators.CurrencyValidator;
import com.teknokote.ess.dto.CurrencyDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
public class CurrencyService extends GenericCheckedService<Long, CurrencyDto>
{
   @Autowired
   private CurrencyDao dao;
   @Autowired
   private CurrencyValidator validator;

}
