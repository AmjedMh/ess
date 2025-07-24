package com.teknokote.ess.core.dao;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.CountryMapper;
import com.teknokote.ess.core.model.Country;
import com.teknokote.ess.core.repository.CountryRepository;
import com.teknokote.ess.dto.CountryDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@Getter
public class CountryDao extends JpaGenericDao<Long, CountryDto, Country>
{
   @Autowired
   private CountryMapper mapper;
   @Autowired
   private CountryRepository repository;
}
