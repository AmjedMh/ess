package com.teknokote.ess.core.dao;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.ControllerPtsMapper;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.repository.ControllePtsRepository;
import com.teknokote.ess.dto.ControllerPtsDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Getter
public class ControllerDao extends JpaGenericDao<Long, ControllerPtsDto, ControllerPts>
{
   @Autowired
   private ControllerPtsMapper mapper;
   @Autowired
   private ControllePtsRepository repository;

   /**
    * Recherche un controlleur à travers son identifiant PTS
    * @param ptsId
    * @return
    */
   public Optional<ControllerPtsDto> findControllerPtsById(String ptsId){
      return Optional.ofNullable(getMapper().toDto(getRepository().findControllerPtsById(ptsId).orElse(null)));
   }
}
