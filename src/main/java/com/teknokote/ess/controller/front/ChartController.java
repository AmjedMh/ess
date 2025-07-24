package com.teknokote.ess.controller.front;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.impl.TankMeasurementServices;
import com.teknokote.ess.core.service.impl.TankService;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.dto.charts.ReportTankMeasurementAndLevelChartDto;
import com.teknokote.ess.dto.charts.TankMeasurementChartDto;
import com.teknokote.ess.dto.TankConfigDto;
import com.teknokote.ess.dto.charts.TankLevelPerSalesChartDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping(EndPoints.CHART_ROOT)
public class ChartController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TankService tankService;
    @Autowired
    private TankMeasurementServices tankMeasurementServices;

    @GetMapping(EndPoints.CHART_FUEL_GRADE_BY_PUMP_AND_PERIOD)
    public <T> List<T> chartTransaction(@RequestParam(defaultValue = "all") String fuel,
                                    @RequestParam(defaultValue = "all") String pump,
                                    @RequestParam(defaultValue = "amount") String chartType,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    @PathVariable Long idCtr) {

        LocalDateTime startDate2 = null;
        LocalDateTime endDate2 = null;
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            startDate2 = LocalDateTime.parse(startDate);
            endDate2 = LocalDateTime.parse(endDate);
        }

        return transactionService.chartOfSalesType(idCtr, chartType, pump, fuel, startDate2, endDate2);
    }

    @GetMapping(EndPoints.CHART_TANK_LEVEL_BY_PERIOD)
    public List<TankLevelPerSalesChartDto> chartTankLevelByDateTank(@PathVariable Long idCtr,
                                                                    @RequestParam(defaultValue = "1") String tank,
                                                                    @RequestParam(required = false) String startDate,
                                                                    @RequestParam(required = false) String endDate) {
        LocalDateTime startDate2 = null;
        LocalDateTime endDate2 = null;
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            startDate2 = LocalDateTime.parse(startDate);
            endDate2 = LocalDateTime.parse(endDate);
        }
        return tankService.tankLevelChangesChart(idCtr, tank, startDate2, endDate2);
    }
    @GetMapping(EndPoints.CHART_TANK_MEASUREMENT_BY_PERIOD)
    public List<TankMeasurementChartDto> chartTankMeasurementByDateTank(@PathVariable Long idCtr,
                                                                        @RequestParam(defaultValue = "1") String tank,
                                                                        @RequestParam(required = false) String startDate,
                                                                        @RequestParam(required = false) String endDate) {
        LocalDateTime startDate2 = null;
        LocalDateTime endDate2 = null;
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            startDate2 = LocalDateTime.parse(startDate);
            endDate2 = LocalDateTime.parse(endDate);
        }
        return tankMeasurementServices.reportTankMeasurementsByTankChart(idCtr, tank, startDate2, endDate2);
    }

    /**
     * Renvoi les séries de données mesure, level (sonde) par date
     * @param idCtr
     * @param tank
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping(EndPoints.CHART_TANK_MEASUREMENT_AND_LEVEL_BY_PERIOD)
    public List<ReportTankMeasurementAndLevelChartDto> chartTankMeasurementAndLevelByDateTank(@PathVariable Long idCtr,
                                                                                        @RequestParam(defaultValue = "1") String tank,
                                                                                        @RequestParam(required = false) String startDate,
                                                                                        @RequestParam(required = false) String endDate) {

        LocalDateTime startDateTime = startDate != null && !startDate.isEmpty() ? LocalDateTime.parse(startDate) : null;
        LocalDateTime endDateTime = endDate != null && !endDate.isEmpty() ? LocalDateTime.parse(endDate) : null;

        List<TankMeasurementChartDto> measurementData = tankMeasurementServices.reportTankMeasurementsByTankChart(idCtr, tank, startDateTime, endDateTime);
        List<TankLevelPerSalesChartDto> levelData = tankService.tankLevelChangesChart(idCtr, tank, startDateTime, endDateTime);
        return tankMeasurementServices.alignAndFillMissingData(measurementData,levelData);
    }

    @GetMapping(EndPoints.LIST_OF_TANK_BY_ID_CONF)
    public List<TankConfigDto> getAllTankByIdC(@PathVariable Long idCtr) {

        return tankService.findTankByControllerOnCurrentConfiguration(idCtr).stream()
                .map(tank -> tankService.mapToTankConfigDto(tank)).toList();
    }
}
