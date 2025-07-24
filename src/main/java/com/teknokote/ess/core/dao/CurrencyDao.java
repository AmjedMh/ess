package com.teknokote.ess.core.dao;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.CurrencyMapper;
import com.teknokote.ess.core.model.Currency;
import com.teknokote.ess.core.repository.CurrencyRepository;
import com.teknokote.ess.dto.CurrencyDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@Getter
public class CurrencyDao extends JpaGenericDao<Long, CurrencyDto, Currency>
{
   @Autowired
   private CurrencyMapper mapper;
   @Autowired
   private CurrencyRepository repository;
}
