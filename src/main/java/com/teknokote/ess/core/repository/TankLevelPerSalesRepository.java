package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.movements.TankLevelPerSales;
import com.teknokote.ess.dto.charts.TankLevelPerSalesChartDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TankLevelPerSalesRepository extends JpaRepository<TankLevelPerSales, Long> {
    @Query("SELECT t FROM TankLevelPerSales t where t.tank = :tank and t.controllerPts.id = :idCtr ORDER BY t.dateTime DESC limit 1")
    Optional<TankLevelPerSales> getLastTankLevelPerSales(Long tank, Long idCtr);

    @Query("SELECT t FROM TankLevelPerSales t where t.tank= :tank and t.controllerPts.id= :idCtr ORDER BY t.dateTime desc")
    List<TankLevelPerSales> getTankLeveChangesByTank(Long idCtr, String tank);

    @Query("SELECT new com.teknokote.ess.dto.charts.TankLevelPerSalesChartDto( " +
            "t.tank, t.tankVolumeChanges, t.dateTime) " +
            "FROM TankLevelPerSales t " +
            "WHERE t.controllerPts.id = :idCtr AND t.dateTime BETWEEN :startDate AND :endDate AND t.tank = :tank " +
            "ORDER BY t.dateTime ASC")
    List<TankLevelPerSalesChartDto> findAllByControllerPtsIdAndTankPeriod(Long idCtr, String tank, LocalDateTime startDate, LocalDateTime endDate);


    @Query("SELECT t FROM TankLevelPerSales t where t.controllerPts.id= :idCtr and  t.pumpTransactionId= :transactionId and t.tank = :tank ORDER BY t.dateTime desc")
    TankLevelPerSales findTankLevelPerSalesByPumpTransactionId(Long idCtr, String tank, Long transactionId);
    @Query("SELECT t FROM TankLevelPerSales t where t.controllerPts.ptsId= :ptsId and  t.tank= :tankId and t.dateTime > :startDateTime and t.dateTime < :endDateTime ORDER BY t.dateTime asc")
    List<TankLevelPerSales> findByTankAndDateRange(String ptsId, Long tankId,LocalDateTime startDateTime ,LocalDateTime endDateTime);
    @Query("SELECT t FROM TankLevelPerSales t where t.controllerPts.ptsId= :ptsId and  t.tank= :tankId and t.dateTime > :endDateTime ORDER BY t.dateTime asc")
    List<TankLevelPerSales> findByTankAndDate(String ptsId, Long tankId,LocalDateTime endDateTime);
}
