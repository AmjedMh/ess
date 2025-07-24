package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Tank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TankRepository extends JpaRepository<Tank,Long> {

    Tank findAllByIdConfAndControllerPtsConfiguration(Long idConf, ControllerPtsConfiguration controllerPtsConfiguration);

    @Query("select p from Tank p join p.controllerPtsConfiguration conf where conf = :controllerPtsConfiguration")
    List<Tank> findTankByControllerConfiguration(ControllerPtsConfiguration controllerPtsConfiguration);
    @Query("select p from Tank p join p.controllerPtsConfiguration conf where conf = :controllerPtsConfiguration and conf.ptsId = :ptsId and  p.idConf= :idConf")
    Tank findByIdConfAndControllerPtsConfigurationAndPtsId(ControllerPtsConfiguration controllerPtsConfiguration,String ptsId,Long idConf);

}
