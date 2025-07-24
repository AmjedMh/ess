package com.teknokote.ess.core.repository.transactions;

import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.dto.charts.FirstIndexSales;
import com.teknokote.ess.dto.data.FuelDataIndexStart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<PumpTransaction, Long>, CustomTransactionRepository {
    @Query("select t from PumpTransaction t join t.controllerPtsConfiguration conf where t.transactionReference = :transaction and t.pump.idConf = :idPump and t.nozzle.idConf = :idConfNozzle and conf.ptsId= :ptsid and t.totalVolume = :totalVolume")
    Optional<PumpTransaction> findByReferenceAndPumpAndNozzleAndPtsIdAndTotalVolume(Long transaction, Long idPump, Long idConfNozzle, String ptsid, BigDecimal totalVolume);

    @Query("SELECT new com.teknokote.ess.dto.data.FuelDataIndexStart(t.totalVolume, t.totalAmount) FROM PumpTransaction t " +
            "WHERE t.pump.idConf = :pumpId " +
            "AND t.controllerPts.ptsId = :ptsId " +
            "AND t.fuelGrade.idConf = :fuelIdConf " +
            "AND t.dateTime < :startDate " +
            "ORDER BY t.dateTime Desc limit 1")
    FuelDataIndexStart findInitialIndex(Long pumpId, String ptsId, Long fuelIdConf, LocalDateTime startDate);

    /**
     * Statistique of sales by Grades and Pump And Period
     */
    @Query("SELECT new com.teknokote.ess.dto.charts.FirstIndexSales(t.amount, t.totalAmount) FROM PumpTransaction t " +
            "WHERE t.pump.idConf = :pumpId " +
            "AND t.controllerPts.id = :idCtr " +
            "AND t.fuelGrade.idConf = :fuelIdConf " +
            "AND t.dateTime >= :startDate AND t.dateTime < :endDate " +
            "ORDER BY t.dateTime ASC limit 1")
    FirstIndexSales findTotalAmountStartForGradeAndPumpByDate(
            Long pumpId, Long idCtr, Long fuelIdConf, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t.totalAmount FROM PumpTransaction t " +
            "WHERE t.pump.idConf = :pumpId " +
            "AND t.controllerPts.id = :idCtr " +
            "AND t.fuelGrade.idConf = :fuelIdConf " +
            "AND t.dateTime > :startDate and t.dateTime <= :endDate " +
            "ORDER BY t.dateTime DESC LIMIT 1")
    BigDecimal findTotalAmountEndForGradeAndPump(Long pumpId, Long idCtr, Long fuelIdConf, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t.amount FROM PumpTransaction t " +
            "WHERE t.pump.idConf = :pumpId " +
            "AND t.controllerPts.id = :idCtr " +
            "AND t.fuelGrade.idConf = :fuelIdConf " +
            "AND t.dateTime <= :endDate and t.dateTime > :startDate " +
            "ORDER BY t.dateTime DESC LIMIT 1")
    Double findLastAmountForGradeAndPump(Long pumpId, Long idCtr, Long fuelIdConf, LocalDateTime startDate, LocalDateTime endDate);

    @Query("select pt from PumpTransaction pt where pt.controllerPtsConfiguration.ptsId=  :ptsId and pt.nozzle.idConf = :nozzleId and pt.pump.idConf = :pumpId and pt.dateTime<= :dateTime order by pt.dateTime DESC limit 1")
    Optional<PumpTransaction> findFirstTransactionOnDate(String ptsId, Long nozzleId, Long pumpId, LocalDateTime dateTime);

    @Query("select pt from PumpTransaction pt where pt.controllerPtsConfiguration.ptsId=  :ptsId and pt.nozzle.idConf = :nozzleId and pt.pump.idConf = :pumpId and pt.dateTime<= :dateTime order by pt.dateTime DESC limit 1")
    Optional<PumpTransaction> findLastTransactionOnDate(String ptsId, Long nozzleId, Long pumpId, LocalDateTime dateTime);

    @Query("select pt from PumpTransaction pt where pt.controllerPtsConfiguration.ptsId=  :ptsId and pt.nozzle.idConf = :nozzleId and pt.pump.idConf = :pumpId and pt.tag = :tag and pt.dateTime<:dateTime order by pt.dateTime Desc limit 1")
    Optional<PumpTransaction> findLastTransactionOnDateByTag(String ptsId, Long nozzleId, Long pumpId, String tag, LocalDateTime dateTime);

    @Query("select pt from PumpTransaction pt where pt.controllerPtsConfiguration.ptsId=  :ptsId and pt.nozzle.idConf = :nozzleId and pt.pump.idConf = :pumpId and pt.tag = :tag and pt.dateTime<:dateTime order by pt.dateTime Desc limit 1")
    Optional<PumpTransaction> findFirstTransactionOnDateByTag(String ptsId, Long nozzleId, Long pumpId, String tag, LocalDateTime dateTime);

    @Query(value = """
    WITH filtered_transactions AS (
        SELECT 
            pt.pump_id, 
            p.id_conf AS pump_id_conf, 
            fg.id_conf AS fuel_id_conf, 
            pt.total_amount, 
            pt.amount, 
            pt.date_time, 
            pt.controller_pts_id, 
            TO_CHAR(pt.date_time, :unit) AS time_unit
        FROM pump_transaction pt
        JOIN pump p ON pt.pump_id = p.id 
        JOIN fuel_grade fg ON pt.fuel_grade_id = fg.id 
        WHERE pt.controller_pts_id = :controllerPtsId 
          AND pt.date_time BETWEEN :startDate AND :endDate 
          AND p.id_conf = COALESCE(:pumpIdConf, p.id_conf) 
          AND fg.id_conf = COALESCE(:fuelIdConf, fg.id_conf)
    ),
    aggregated_transactions AS (
        SELECT 
            pump_id_conf, 
            fuel_id_conf, 
            controller_pts_id, 
            time_unit, 
            FIRST_VALUE(total_amount) OVER (PARTITION BY pump_id_conf, fuel_id_conf, controller_pts_id, time_unit 
                                            ORDER BY date_time ASC) AS first_total_amount, 
            LAST_VALUE(total_amount) OVER (PARTITION BY pump_id_conf, fuel_id_conf, controller_pts_id, time_unit 
                                           ORDER BY date_time ASC 
                                           ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_total_amount, 
            FIRST_VALUE(amount) OVER (PARTITION BY pump_id_conf, fuel_id_conf, controller_pts_id, time_unit 
                                      ORDER BY date_time ASC) AS first_amount 
        FROM filtered_transactions
    )
    SELECT 
        pump_id_conf, 
        fuel_id_conf, 
        controller_pts_id, 
        time_unit, 
        first_total_amount, 
        last_total_amount, 
        first_amount, 
        first_amount + last_total_amount - first_total_amount AS sales 
    FROM aggregated_transactions 
    GROUP BY 
        pump_id_conf, 
        fuel_id_conf, 
        controller_pts_id, 
        time_unit, 
        first_total_amount, 
        last_total_amount, 
        first_amount 
    ORDER BY time_unit ASC, pump_id_conf, fuel_id_conf
""", nativeQuery = true)
    List<Object[]> getAggregatedAmountSales(
            @Param("controllerPtsId") Long controllerPtsId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("pumpIdConf") Long pumpIdConf,
            @Param("fuelIdConf") Long fuelIdConf,
            @Param("unit") String unit
    );

    @Query(value = """
    WITH filtered_transactions AS (
        SELECT 
            pt.pump_id, 
            p.id_conf AS pump_id_conf, 
            fg.name AS fuel_name, 
            pt.total_volume, 
            pt.volume, 
            pt.date_time, 
            pt.controller_pts_id, 
            TO_CHAR(pt.date_time, :unit) AS time_unit
        FROM pump_transaction pt
        JOIN pump p ON pt.pump_id = p.id 
        JOIN fuel_grade fg ON pt.fuel_grade_id = fg.id 
        WHERE pt.controller_pts_id = :controllerPtsId 
          AND pt.date_time BETWEEN :startDate AND :endDate 
          AND p.id_conf = COALESCE(:pumpIdConf, p.id_conf) 
          AND fg.name = COALESCE(:fuelName, fg.name)
    ),
    aggregated_transactions AS (
        SELECT 
            pump_id_conf, 
            fuel_name, 
            controller_pts_id, 
            time_unit, 
            FIRST_VALUE(total_volume) OVER (PARTITION BY pump_id_conf, fuel_name, controller_pts_id, time_unit 
                                            ORDER BY date_time ASC) AS first_total_volume, 
            LAST_VALUE(total_volume) OVER (PARTITION BY pump_id_conf, fuel_name, controller_pts_id, time_unit 
                                           ORDER BY date_time ASC 
                                           ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_total_volume, 
            FIRST_VALUE(volume) OVER (PARTITION BY pump_id_conf, fuel_name, controller_pts_id, time_unit 
                                      ORDER BY date_time ASC) AS first_volume 
        FROM filtered_transactions
    )
    SELECT 
        fuel_name, 
        time_unit, 
        first_volume + last_total_volume - first_total_volume AS sales 
    FROM aggregated_transactions 
    GROUP BY 
        fuel_name, 
        time_unit,
        first_volume,
        last_total_volume,
        first_total_volume
    ORDER BY time_unit ASC
""", nativeQuery = true)
    List<Object[]> getAggregatedVolumeSales(
            @Param("controllerPtsId") Long controllerPtsId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("pumpIdConf") Long pumpIdConf,
            @Param("fuelName") String fuelName,
            @Param("unit") String unit
    );

    @Query(value = """
    WITH filtered_transactions AS (
        SELECT 
            pt.pump_id, 
            p.id_conf AS pump_id_conf, 
            fg.name AS fuel_name, 
            pt.total_volume, 
            pt.volume, 
            pt.total_amount,
            pt.amount,
            pt.date_time, 
            pt.controller_pts_id 
        FROM pump_transaction pt
        JOIN pump p ON pt.pump_id = p.id 
        JOIN fuel_grade fg ON pt.fuel_grade_id = fg.id 
        WHERE pt.controller_pts_id = :controllerPtsId 
          AND pt.date_time BETWEEN :startDate AND :endDate 
          AND p.id_conf = COALESCE(:pumpIdConf, p.id_conf) 
          AND fg.name = COALESCE(:fuelName, fg.name)
    ),
    aggregated_transactions AS (
        SELECT 
            pump_id_conf, 
            fuel_name, 
            controller_pts_id,  
            FIRST_VALUE(total_volume) OVER (PARTITION BY pump_id_conf, fuel_name, controller_pts_id
                                            ORDER BY date_time ASC) AS first_total_volume, 
            LAST_VALUE(total_volume) OVER (PARTITION BY pump_id_conf, fuel_name, controller_pts_id
                                           ORDER BY date_time ASC 
                                           ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_total_volume, 
            FIRST_VALUE(volume) OVER (PARTITION BY pump_id_conf, fuel_name, controller_pts_id
                                      ORDER BY date_time ASC) AS first_volume ,
            FIRST_VALUE(total_amount) OVER (PARTITION BY pump_id_conf, fuel_name, controller_pts_id
                                            ORDER BY date_time ASC) AS first_total_amount, 
            LAST_VALUE(total_amount) OVER (PARTITION BY pump_id_conf, fuel_name, controller_pts_id
                                           ORDER BY date_time ASC 
                                           ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_total_amount, 
            FIRST_VALUE(amount) OVER (PARTITION BY pump_id_conf, fuel_name, controller_pts_id
                                      ORDER BY date_time ASC) AS first_amount 
        FROM filtered_transactions
    )
    SELECT 
        pump_id_conf, 
        fuel_name, 
        controller_pts_id,  
        first_volume + last_total_volume - first_total_volume AS volume_sales ,
        first_amount + last_total_amount - first_total_amount AS amount_sales 
    FROM aggregated_transactions 
    GROUP BY 
        pump_id_conf, 
        fuel_name, 
        controller_pts_id ,
        first_total_volume, 
        last_total_volume, 
        first_volume, 
        first_total_amount, 
        last_total_amount, 
        first_amount 
    ORDER BY  pump_id_conf, fuel_name
""", nativeQuery = true)
    List<Object[]> getVolumeSalesforPeriod(
            @Param("controllerPtsId") Long controllerPtsId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("pumpIdConf") Long pumpIdConf,
            @Param("fuelName") String fuelName
    );

    @Query("SELECT sum (t.volume)FROM PumpTransaction t " +
            "WHERE t.controllerPts.ptsId = :ptsId " +
            "AND t.nozzle.tank.idConf = :tankIdConf " +
            "AND t.dateTime >= :startDate and t.dateTime <= :endDate ")
    Double findSalesVolumeByDateAndTank(String ptsId, Long tankIdConf, LocalDateTime startDate, LocalDateTime endDate);

}
