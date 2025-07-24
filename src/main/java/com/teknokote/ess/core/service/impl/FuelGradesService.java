package com.teknokote.ess.core.service.impl;


import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.service.base.EntityService;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.pts.client.response.JsonPTSResponse;

import java.util.List;


public interface FuelGradesService extends EntityService<FuelGrade, Long> {

    void addNewFuelGrades(ControllerPtsConfiguration controllerPtsConfiguration, JsonPTSResponse response);

    FuelGradeConfigDto mapToFuelGradeConfigDto(FuelGrade fuelGrade);

    List<FuelGrade> findFuelGradesByControllerOnCurrentConfiguration(Long idCtr);

    FuelGrade findFuelGradeByConfId(Long idConf);

    FuelGrade findAllByIdConfAndControllerPtsConfiguration(Long fuelGradeId, ControllerPtsConfiguration controllerPtsConfiguration);
    FuelGrade findFuelGradeByIdConfAndController(Long idConf, String configurationId, Long idCtr);
    }
