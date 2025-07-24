package com.teknokote.ess.core.repository.tank_delivery;

import com.teknokote.ess.core.model.movements.TankDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TankDeliveryRepository extends JpaRepository<TankDelivery, Long>, CustomTankDeliveryRepository {
    @Query("SELECT td FROM TankDelivery td " +
            "WHERE td.tank.idConf = :tank " +
            "AND td.controllerPts.id = :idCtr " +
            "AND td.startDateTime = (SELECT MAX(tdd.startDateTime) " +
            "                 FROM TankDelivery tdd " +
            "                 WHERE tdd.tank.idConf = :tank " +
            "                 AND tdd.controllerPts.id = :idCtr)")
    TankDelivery findLastDeliveriesByTankIdConf(Long idCtr, Long tank);
    @Query("SELECT td FROM TankDelivery td " +
            "WHERE td.tank.idConf = :tank " +
            "AND td.controllerPts.ptsId = :ptsId " +
            "AND td.startDateTime IS NOT NULL " +
            "AND td.startDateTime < :dateTime " +
            "ORDER BY td.startDateTime DESC limit 1")
    Optional<TankDelivery> findLastDeliveryByTankIdConfAndPtsId(String ptsId, Long tank, LocalDateTime dateTime);

    @Query("SELECT t FROM TankDelivery t where t.tank.idConf= :tankIdConf and t.configurationId = :configurationId ORDER BY t.startDateTime desc limit 1 ")
    Optional<TankDelivery> getLastDelivery(Long tankIdConf, String configurationId);

    @Query("SELECT td FROM TankDelivery td " +
            "WHERE td.tank.idConf = :tank " +
            "AND td.controllerPts.ptsId = :ptsId " +
            "AND td.startDateTime IS NOT NULL " +
            "AND td.startDateTime > :dateTime " +
            "ORDER BY td.startDateTime asc limit 1")
    Optional<TankDelivery> findNextDeliveryByTankIdConfAndPtsId(String ptsId, Long tank, LocalDateTime dateTime);
}
