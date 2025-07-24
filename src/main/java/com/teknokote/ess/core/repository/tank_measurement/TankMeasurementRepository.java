package com.teknokote.ess.core.repository.tank_measurement;

import com.teknokote.ess.core.model.movements.TankMeasurement;
import com.teknokote.ess.dto.charts.TankMeasurementChartDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TankMeasurementRepository extends JpaRepository<TankMeasurement, Long>,CustomTankMeasurementRepository {
    @Query("SELECT t FROM TankMeasurement t where t.controllerPts.ptsId= :ptsId and t.tank = :tankIdConf  ORDER BY t.dateTime desc limit 1 ")
    Optional<TankMeasurement> getLastMeasurement(String ptsId, Long tankIdConf);
    @Query("SELECT t FROM TankMeasurement t where t.controllerPts.ptsId= :ptsId and t.tank = :tankIdConf and t.dateTime < :dateTime ORDER BY t.dateTime desc limit 1 ")
    Optional<TankMeasurement> getLastMeasurementByDate(String ptsId, Long tankIdConf,LocalDateTime dateTime);


    @Query("SELECT new com.teknokote.ess.dto.charts.TankMeasurementChartDto( " +
            "t.dateTime, t.tank, t.productVolume) " +
            "FROM TankMeasurement t " +
            "WHERE t.controllerPts.id = :idCtr AND t.dateTime BETWEEN :startDate AND :endDate AND t.tank = :tank " +
            "ORDER BY t.dateTime ASC")
    List<TankMeasurementChartDto> findAllByControllerPtsIdAndTankIdAndPeriod(Long idCtr, Long tank, LocalDateTime startDate, LocalDateTime endDate);
}
