package com.teknokote.ess.core.repository;


import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelGradesRepository extends JpaRepository<FuelGrade, Long> {

    FuelGrade findAllByIdConfAndControllerPtsConfiguration(Long idConf, ControllerPtsConfiguration controllerPtsConfiguration);

    @Query("select p from FuelGrade p join p.controllerPtsConfiguration conf where conf= :controllerPtsConfiguration")
    List<FuelGrade> findFuelGradesByControllerConfiguration(ControllerPtsConfiguration controllerPtsConfiguration);

    @Query("select p from FuelGrade p join p.controllerPtsConfiguration conf where conf= :controllerPtsConfiguration and p.idConf= :idConf")
    FuelGrade findFuelGradesByControllerConfigurationAndIdConf(ControllerPtsConfiguration controllerPtsConfiguration, Long idConf);

    @Query("select p from FuelGrade p join p.controllerPtsConfiguration conf where conf= :controllerPtsConfiguration and p.id= :fuelId")
    FuelGrade findFuelGradesByControllerConfigurationAndId(ControllerPtsConfiguration controllerPtsConfiguration, Long fuelId);

    @Query("SELECT t FROM FuelGrade t WHERE t.idConf = :idConf AND t.controllerPtsConfiguration.configurationId = :configurationId")
    FuelGrade findFuelGradeByConfId(Long idConf);

    @Query("SELECT t FROM FuelGrade t WHERE t.idConf = :idConf AND t.controllerPtsConfiguration.configurationId = :configurationId and t.controllerPts.id = :idCtr")
    FuelGrade findFuelGradeByIdConfAndController(Long idConf, String configurationId, Long idCtr);


}
