package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.Alert;
import com.teknokote.ess.core.model.configuration.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert,Long> {

    @Query("SELECT a.controllerPts.station FROM Alert a WHERE a.controllerPts.id= :controllerId")
    Station findStationByAlertId(Long controllerId );
}
