package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.CountryDao;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.service.impl.validators.CountryValidator;
import com.teknokote.ess.dto.CountryDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
public class CountryService extends GenericCheckedService<Long, CountryDto>
{
   @Autowired
   private CountryDao dao;
   @Autowired
   private CountryValidator validator;

   private List<CountryDto> countriesCache;

   @PostConstruct
   public void prepareListCache(){
      countriesCache = super.findAll().stream().sorted((c1,c2)->c1.getName().compareToIgnoreCase(c2.getName())).toList();
   }

   @Override
   public List<CountryDto> findAll()
   {
      return countriesCache;
   }
}
