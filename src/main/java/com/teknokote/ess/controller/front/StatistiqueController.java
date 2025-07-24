package com.teknokote.ess.controller.front;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.model.movements.TankDelivery;
import com.teknokote.ess.core.service.impl.FuelGradesServiceImpl;
import com.teknokote.ess.core.service.impl.TankDeliveryService;
import com.teknokote.ess.core.service.impl.TankMeasurementServices;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.dto.SalesDto;
import com.teknokote.ess.dto.TankDeliveryDto;
import com.teknokote.ess.dto.charts.SalesGradesByPump;
import com.teknokote.ess.dto.charts.SalesGradesDto;
import com.teknokote.pts.client.upload.dto.MeasurementDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin("*")
@Slf4j
@RequestMapping(EndPoints.STAT_ROOT)
public class StatistiqueController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private FuelGradesServiceImpl fuelGradesService;
    @Autowired
    private TankDeliveryService tankDeliveryService;
    @Autowired
    private TankMeasurementServices tankMeasurementServices;

    @GetMapping(EndPoints.STAT_TANK_MEASUREMENT)
    public List<MeasurementDto> getAllTankStat(@PathVariable String ptsId) {
        return tankMeasurementServices.getAllTankMeasurementsSortedByTank(ptsId);
    }

    @GetMapping(EndPoints.LAST_DELIVERY)
    public TankDeliveryDto getLastDelivery(@PathVariable Long idCtr, @PathVariable Long tank) {
        TankDelivery tankDelivery = tankDeliveryService.findLatestTankDeliveryByTankId(idCtr, tank);
        return tankDeliveryService.mapDeliveryToDto(tankDelivery);
    }

    @GetMapping(EndPoints.GET_ALL_SALES_BY_CONTROLLER)
    public List<SalesDto> getSalesByController(@PathVariable Long idCtr,
                                               @RequestParam(required = false) LocalDateTime startDate,
                                               @RequestParam(required = false) LocalDateTime endDate) {

        return transactionService.getSalesByController(idCtr, startDate, endDate);
    }

    @GetMapping(EndPoints.GET_ALL_SALES_BY_PUMP)
    public List<SalesGradesByPump> getSalesByPumpAndGrades(@PathVariable Long idCtr,
                                                           @PathVariable Long pumpId,
                                                           @RequestParam(required = false) LocalDateTime startDate,
                                                           @RequestParam(required = false) LocalDateTime endDate) {

        return transactionService.getSalesByGradesAndPump(idCtr, pumpId, startDate, endDate);
    }

    @GetMapping(EndPoints.GET_ALL_SALES_BY_GRADES)
    public List<SalesGradesDto> getSalesGrades(@PathVariable Long idCtr,
                                               @RequestParam(required = false) LocalDateTime startDate,
                                               @RequestParam(required = false) LocalDateTime endDate) {

        return fuelGradesService.getSalesByGrades(idCtr, startDate, endDate);
    }
}
