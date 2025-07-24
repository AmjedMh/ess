package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.configuration.Nozzle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NozzleRepository extends JpaRepository<Nozzle,Long> {

    @Query("select n from Nozzle n where n.idConf = :nozzleIdConf and n.pump.idConf= :pumpIdConf and n.controllerPtsConfiguration.configurationId= :configurationId and n.controllerPts.ptsId= :ptsId")
    Nozzle findByIdConfAndPumpAndConfigurationIdAndControllerPts(Long nozzleIdConf, Long pumpIdConf, String configurationId, String ptsId);
    @Query("select n.grade from Nozzle n where n.idConf = :nozzleIdConf and n.pump.idConf= :pumpIdConf and n.controllerPtsConfiguration.configurationId= :configurationId and n.controllerPts.ptsId= :ptsId")
    FuelGrade findFuelByNozzleAndPump(Long nozzleIdConf, Long pumpIdConf, String configurationId, String ptsId);
    @Query("select n.grade from Nozzle n where n.pump.idConf= :pumpIdConf and n.controllerPtsConfiguration.configurationId= :configurationId and n.controllerPts.ptsId= :ptsId")
    List<FuelGrade> findAllFuelGradesByPump(Long pumpIdConf, String configurationId, String ptsId);

    @Query("select n from Nozzle n where n.idConf = :nozzleIdConf and n.controllerPtsConfiguration = :controllerPtsConfiguration and n.pump.idConf= :pumpIdConf ")
    Nozzle findNozzleByIdConfAndControllerPtsConfigurationAndPump(Long nozzleIdConf,ControllerPtsConfiguration controllerPtsConfiguration,Long pumpIdConf);

    @Query("SELECT n FROM Nozzle n WHERE n.controllerPts.id = :idCtr AND n.pump.idConf = :pumpIdConf AND n.controllerPtsConfiguration.id = :currentConfigId ORDER BY n.idConf, n.pump.idConf")
    List<Nozzle> findNozzleByPump(Long idCtr,Long currentConfigId, Long pumpIdConf);

    @Query("select p from Nozzle p where p.controllerPtsConfiguration= :controllerPtsConfiguration")
    List<Nozzle> findNozzlesByControllerConfiguration(ControllerPtsConfiguration controllerPtsConfiguration);
    List<Nozzle> findAllByPump_IdConfAndAndControllerPtsConfiguration_ConfigurationId(Long pumpIdConf, String configurationId);
    @Query("select n.grade.idConf from Nozzle n where n.pump.idConf = :pumpIdConf and n.controllerPtsConfiguration.configurationId = :configurationId and n.controllerPtsConfiguration.ptsId = :ptsId")
    List<Long> findAllFuelIdsByConfigurationAndPumpId(Long pumpIdConf,String configurationId,String ptsId);
    @Query("select n.grade.name from Nozzle n where n.pump.idConf = :pumpIdConf and n.controllerPtsConfiguration.configurationId = :configurationId and n.controllerPtsConfiguration.ptsId = :ptsId")
    List<String> findAllFuelByConfigurationAndPumpId(Long pumpIdConf,String configurationId,String ptsId);
    @Query("SELECT DISTINCT n.pump.idConf FROM Nozzle n WHERE n.grade.name = :fuelGradeName and n.controllerPtsConfiguration = :controllerPtsConfiguration")
    List<Long> findPumpsByFuelGrade(String fuelGradeName,ControllerPtsConfiguration controllerPtsConfiguration);

}
