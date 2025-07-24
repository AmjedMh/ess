package com.teknokote.ess.core.service.impl;


import com.teknokote.ess.core.dao.ControllerDao;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.service.impl.validators.ControllerValidator;
import com.teknokote.ess.dto.ControllerPtsDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Getter
public class ControllerService extends GenericCheckedService<Long, ControllerPtsDto>
{
    @Autowired
    private ControllerValidator validator;
    @Autowired
    private ControllerDao dao;


    /**
     * Renvoie le contrôleur identifié par le ptsId
     */
    public Optional<ControllerPtsDto> findControllerDto(String ptsId){
        return dao.findControllerPtsById(ptsId);
    }

    /**
     * NOT TO BE USED
     * A n'utiliser que pour les Upload
     * @param ptsId
     * @return
     */
    public ControllerPts findController(String ptsId){
        return dao.getRepository().findControllerPtsById(ptsId).orElse(null);
    }
}
