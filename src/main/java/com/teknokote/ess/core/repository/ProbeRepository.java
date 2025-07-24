package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Probe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProbeRepository extends JpaRepository<Probe,Long> {

    Probe findAllByIdConfAndControllerPtsConfiguration(Long idConf, ControllerPtsConfiguration controllerPtsConfiguration);
    @Query("select p from Probe p join p.controllerPtsConfiguration conf where conf= :controllerPtsConfiguration")
    List<Probe> findProbesByControllerOnCurrentConfiguration(ControllerPtsConfiguration controllerPtsConfiguration);
}

