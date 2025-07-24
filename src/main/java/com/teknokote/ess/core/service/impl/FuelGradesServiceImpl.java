package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.repository.FuelGradesRepository;
import com.teknokote.ess.core.repository.transactions.TransactionRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.charts.SalesGradesDto;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSFuelGrade;
import com.teknokote.pts.client.response.configuration.PTSFuelGradesConfigurationResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSFuelGradesConfigurationResponsePacketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FuelGradesServiceImpl extends AbstractEntityService<FuelGrade, Long> implements FuelGradesService {
    @Autowired
    private FuelGradesRepository fuelGradesRepository;
    @Autowired
    private PumpService pumpService;
    @Autowired
    private NozzleService nozzleService;
    @Autowired
    private ControllerPtsConfigurationService controllerPtsConfigurationService;
    @Autowired
    private TransactionRepository transactionRepository;
    public FuelGradesServiceImpl(FuelGradesRepository dao,ControllerPtsConfigurationService controllerPtsConfigurationService, TransactionRepository transactionRepository, PumpService pumpService, NozzleService nozzleService) {
        super(dao);
        this.fuelGradesRepository = dao;
        this.controllerPtsConfigurationService = controllerPtsConfigurationService;
        this.transactionRepository = transactionRepository;
        this.pumpService = pumpService;
        this.nozzleService = nozzleService;
    }

    public FuelGrade findFuelGradeByIdConfAndController(Long idConf, String configurationId, Long idCtr){
        return fuelGradesRepository.findFuelGradeByIdConfAndController(idConf,configurationId,idCtr);
    }
    @Override
    public void addNewFuelGrades(ControllerPtsConfiguration controllerPtsConfiguration, JsonPTSResponse response) {
        response.getPackets().stream()
                .map(responsePacket -> ((PTSFuelGradesConfigurationResponsePacket) responsePacket).getData())
                .map(PTSFuelGradesConfigurationResponsePacketData::getFuelGrades)
                .flatMap(Collection::stream)
                .map(fuelGrade -> addFuelGrade(fuelGrade, controllerPtsConfiguration))
                .forEach(fuelGrade -> {});
    }

    public FuelGrade addFuelGrade(PTSFuelGrade fuelGrade, ControllerPtsConfiguration controllerPtsConfiguration) {
        FuelGrade fuelGrades = fuelGradesRepository.findAllByIdConfAndControllerPtsConfiguration(fuelGrade.getId(), controllerPtsConfiguration);
        if (fuelGrades == null)
            fuelGrades = new FuelGrade();
        fuelGrades.setIdConf(fuelGrade.getId());
        fuelGrades.setName(fuelGrade.getName());
        fuelGrades.setPrice(fuelGrade.getPrice());
        fuelGrades.setExpansionCoefficient(fuelGrade.getExpansionCoefficient());
        fuelGrades.setControllerPts(controllerPtsConfiguration.getControllerPts());
        fuelGrades.setControllerPtsConfiguration(controllerPtsConfiguration);
        return fuelGradesRepository.save(fuelGrades);
    }

    public FuelGradeConfigDto mapToFuelGradeConfigDto(FuelGrade fuelGrade) {
        FuelGradeConfigDto fuelGradeConfigDto = new FuelGradeConfigDto();
        fuelGradeConfigDto.setIdConf(fuelGrade.getIdConf());

        fuelGradeConfigDto.setName(fuelGrade.getName());
        fuelGradeConfigDto.setPrice(fuelGrade.getPrice());
        fuelGradeConfigDto.setExpansionCoefficient(fuelGrade.getExpansionCoefficient());
        return fuelGradeConfigDto;
    }

    @Override
    public FuelGrade findFuelGradeByConfId(Long idConf) {
        return fuelGradesRepository.findFuelGradeByConfId(idConf);
    }

    @Override
    public List<FuelGrade> findFuelGradesByControllerOnCurrentConfiguration(Long idCtr) {
        final ControllerPtsConfiguration currentConfigurationOnController = controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr);
        return fuelGradesRepository.findFuelGradesByControllerConfiguration(currentConfigurationOnController);
    }

    @Override
    public FuelGrade findAllByIdConfAndControllerPtsConfiguration(Long fuelGradeId, ControllerPtsConfiguration controllerPtsConfiguration) {
        return fuelGradesRepository.findAllByIdConfAndControllerPtsConfiguration(fuelGradeId, controllerPtsConfiguration);
    }
    /**
     * Retrieves sales data by fuel grade for the given controller, start date, and end date.
     *
     * @param idCtr    The controller ID.
     * @param startDate The start date of the period.
     * @param endDate   The end date of the period.
     * @return A list of SalesGradesDto objects, each representing a fuel grade with its total sales data.
     */
    public List<SalesGradesDto> getSalesByGrades(Long idCtr, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate must not be null");
        }

        // Retrieve the current controller configuration
        ControllerPtsConfiguration configuration = controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr);
        if (configuration ==null) {
            return Collections.emptyList();
        }

        // Get all pump IDs and map them to fuel IDs
        List<Long> pumpIds = pumpService.findPumpIdsByControllerConfiguration(configuration.getConfigurationId(), configuration.getPtsId());
        Map<Long, List<String>> pumpToFuelIdsMap = new HashMap<>();
        for (Long pumpId : pumpIds) {
            List<String> fuelIdsForPump = nozzleService.findAllFuelByConfigurationAndPumpId(pumpId, configuration.getConfigurationId(), configuration.getPtsId());
            pumpToFuelIdsMap.put(pumpId, fuelIdsForPump);
        }

        return processChartSalesByFuelGrade(idCtr,startDate,endDate,pumpToFuelIdsMap);
    }
    List<SalesGradesDto> processChartSalesByFuelGrade(
            Long idCtr,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Map<Long, List<String>> pumpToFuelIdsMap) {

        // Use a map to aggregate results by fuelName
        Map<String, List<Double>> aggregatedSales = new HashMap<>();

        // Process results based on conditions
        for (Map.Entry<Long, List<String>> entry : pumpToFuelIdsMap.entrySet()) {
            Long pumpId = entry.getKey();
            for (String fuelName : entry.getValue()) {
                List<Object[]> queryResults = transactionRepository.getVolumeSalesforPeriod(
                        idCtr, startDate, endDate, pumpId, fuelName);

                // Aggregate query results
                for (Object[] result : queryResults) {
                    Double volume = (Double) result[3];
                    Double amount = (Double) result[4];

                    aggregatedSales.computeIfAbsent(fuelName, k -> Arrays.asList(0.0, 0.0));

                    // Update the totalAmount and totalVolume
                    List<Double> totals = aggregatedSales.get(fuelName);
                    totals.set(0, totals.get(0) + amount); // Update totalAmount
                    totals.set(1, totals.get(1) + volume); // Update totalVolume
                }
            }
        }

        // Convert the aggregated results into DTOs
        return aggregatedSales.entrySet().stream()
                .map(entry -> {
                    String fuelName = entry.getKey();
                    List<Double> totals = entry.getValue();
                    return new SalesGradesDto(fuelName, totals.get(0), totals.get(1));
                })
                .sorted(Comparator.comparing(SalesGradesDto::getFuelGrade).reversed())
                .toList();
    }

}
