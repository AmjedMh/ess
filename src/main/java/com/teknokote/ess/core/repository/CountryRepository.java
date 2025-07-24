package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CountryRepository extends JpaRepository<Country,Long>
{
}
