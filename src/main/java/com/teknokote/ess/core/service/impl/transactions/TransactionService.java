package com.teknokote.ess.core.service.impl.transactions;

import com.teknokote.ess.core.dao.PumpAttendantDao;
import com.teknokote.ess.core.dao.mappers.PumpAttendantMapper;
import com.teknokote.ess.core.dao.mappers.PumpTransactionMapper;
import com.teknokote.ess.core.model.Currency;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.movements.EnumFilter;
import com.teknokote.ess.core.model.movements.EnumTransaction;
import com.teknokote.ess.core.model.movements.EnumTransactionState;
import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.repository.configuration.ControllerPtsConfigurationRepository;
import com.teknokote.ess.core.repository.transactions.TransactionRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import com.teknokote.ess.core.service.impl.FuelGradesService;
import com.teknokote.ess.core.service.impl.NozzleService;
import com.teknokote.ess.core.service.impl.PumpService;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningExecutionService;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningService;
import com.teknokote.ess.dto.PumpAttendantDto;
import com.teknokote.ess.dto.SalesDto;
import com.teknokote.ess.dto.TransactionDto;
import com.teknokote.ess.dto.TransactionFilterDto;
import com.teknokote.ess.dto.charts.ChartAllFuelAndAllPumpDto;
import com.teknokote.ess.dto.charts.ChartFuelAllPumpDto;
import com.teknokote.ess.dto.charts.FirstIndexSales;
import com.teknokote.ess.dto.charts.SalesGradesByPump;
import com.teknokote.ess.dto.data.FuelDataIndexStart;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDetailDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDto;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningExecutionDto;
import com.teknokote.pts.client.upload.pump.JsonPumpUpload;
import com.teknokote.pts.client.upload.pump.PumpUpload;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class TransactionService extends AbstractEntityService<PumpTransaction, Long> {

    @Autowired
    private NozzleService nozzleService;
    @Autowired
    private PumpService pumpService;
    @Autowired
    private FuelGradesService fuelGradesService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private PumpTransactionMapper pumpTransactionMapper;
    @Autowired
    private WorkDayShiftPlanningService workDayShiftPlanningService;
    @Autowired
    private WorkDayShiftPlanningExecutionService workDayShiftPlanningExecutionService;
    @Autowired
    private TankLevelPerSalesService tankLevelPerSalesService;
    @Autowired
    private PumpAttendantDao pumpAttendantDao;
    @Autowired
    private PumpAttendantMapper mapper;
    @Autowired
    private ControllerPtsConfigurationRepository controllerPtsConfigurationRepository;

    public TransactionService(TransactionRepository dao, ControllerPtsConfigurationRepository controllerPtsConfigurationRepository, WorkDayShiftPlanningService workDayShiftPlanningService, NozzleService nozzleService,PumpService pumpService ,WorkDayShiftPlanningExecutionService workDayShiftPlanningExecutionService,PumpAttendantMapper mapper, PumpTransactionMapper pumpTransactionMapper, FuelGradesService fuelGradesService,PumpAttendantDao pumpAttendantDao ,TankLevelPerSalesService tankLevelPerSalesService)     {
        super(dao);
        this.transactionRepository = dao;
        this.workDayShiftPlanningService = workDayShiftPlanningService;
        this.controllerPtsConfigurationRepository = controllerPtsConfigurationRepository;
        this.nozzleService = nozzleService;
        this.pumpService = pumpService;
        this.workDayShiftPlanningExecutionService = workDayShiftPlanningExecutionService;
        this.mapper = mapper;
        this.pumpTransactionMapper = pumpTransactionMapper;
        this.fuelGradesService = fuelGradesService;
        this.pumpAttendantDao = pumpAttendantDao;
        this.tankLevelPerSalesService = tankLevelPerSalesService;
    }
    private static final String DATE_NOT_NULL = "startDate and endDate must not be null";
    /**
     * Intègre la transaction uploadée
     */
    public void processUploadedTransaction(JsonPumpUpload pumpUpload, ControllerPtsConfiguration controllerPtsConfiguration) {
        pumpUpload.getPackets().forEach(packet -> processUploadedTransactionPacket(packet.getData(), controllerPtsConfiguration));
    }
    /**
     * Méthode de traitement des données d'une transaction.
     * Une transaction admet un numéro (champs 'transaction') qui est supposé être unique par contrôleur.
     * La méthode intègre en plus le numéro de la pompe dans l'identification d'une transaction.On pourrait aller plus loin avec le nozzle.
     * Si la transaction existe en base (numéro transaction, numéro pompe) pour le contrôleur {@param ptsId},
     * 1. si son état est finished, ne rien faire
     * 2. Sinon, mettre à jour la transaction et la passer à finished
     */
    public void processUploadedTransactionPacket(PumpUpload pumpUpload, ControllerPtsConfiguration controllerPtsConfiguration) {
        final Optional<PumpTransaction> registredTransaction = transactionRepository.findByReferenceAndPumpAndNozzleAndPtsIdAndTotalVolume(pumpUpload.getTransaction(),
                pumpUpload.getPump(), pumpUpload.getNozzle(), controllerPtsConfiguration.getPtsId(), BigDecimal.valueOf(pumpUpload.getTotalVolume()));
        PumpTransaction pumpTransaction;
        if (registredTransaction.isPresent()) {
            pumpTransaction = registredTransaction.get();
            if (!EnumTransactionState.FINISHED.equals(registredTransaction.get().getState())) {
                // Mettre à jour les champs de la transaction à partir de ceux remontés
                pumpTransactionMapper.updateAndMarkAsFinished(pumpUpload, pumpTransaction);
                pumpTransaction.setTransactionReference(pumpUpload.getTransaction());
                pumpTransaction.setType(EnumTransaction.TRANSACTION);
                pumpTransaction.setConfigurationId(pumpUpload.getConfigurationId());
                final FuelGrade fuelGrade = fuelGradesService.findFuelGradeByConfId(pumpUpload.getFuelGradeId());
                pumpTransaction.setFuelGrade(fuelGrade);
                pumpTransaction.setTag(pumpUpload.getTag());
                Optional<PumpAttendant> pumpAttendant = pumpAttendantDao.findByTag(pumpUpload.getTag(), controllerPtsConfiguration.getControllerPts().getStationId());
                if (pumpAttendant.isPresent()) {
                    pumpTransaction.setPumpAttendant(pumpAttendant.get());
                } else {
                    findPumpAttendantForTransaction(pumpTransaction, controllerPtsConfiguration);

                }
                pumpTransaction.setControllerPtsConfiguration(controllerPtsConfiguration);
                pumpTransaction.setControllerPts(controllerPtsConfiguration.getControllerPts());
                pumpTransaction.setState(EnumTransactionState.FINISHED);
                transactionRepository.save(pumpTransaction);
            }
        } else {
            // Créer une nouvelle transaction
            pumpTransaction = pumpTransactionMapper.fromPumpUploadedData(pumpUpload, controllerPtsConfiguration.getPtsId(), pumpService, nozzleService);
            pumpTransaction.setTransactionReference(pumpUpload.getTransaction());
            pumpTransaction.setType(EnumTransaction.TRANSACTION);
            pumpTransaction.setConfigurationId(pumpUpload.getConfigurationId());
            final FuelGrade fuelGrade = fuelGradesService.findAllByIdConfAndControllerPtsConfiguration(pumpUpload.getFuelGradeId(), controllerPtsConfiguration);
            pumpTransaction.setFuelGrade(fuelGrade);
            pumpTransaction.setTag(pumpUpload.getTag());
            Optional<PumpAttendant> pumpAttendant = pumpAttendantDao.findByTag(pumpUpload.getTag(), controllerPtsConfiguration.getControllerPts().getStationId());
            if (pumpAttendant.isPresent()) {
                pumpTransaction.setPumpAttendant(pumpAttendant.get());
            } else {
                findPumpAttendantForTransaction(pumpTransaction, controllerPtsConfiguration);
            }
            pumpTransaction.setControllerPtsConfiguration(controllerPtsConfiguration);
            pumpTransaction.setControllerPts(controllerPtsConfiguration.getControllerPts());
            pumpTransaction.setState(EnumTransactionState.FINISHED);
            pumpTransaction = transactionRepository.save(pumpTransaction);

            // Update TankDelivery Status and tankDelivery cash
            if (Objects.nonNull(pumpTransaction.getNozzle().getTank())) {
                tankLevelPerSalesService.saveTankLevelChanges(pumpTransaction);
            }
        }
    }

    public void findPumpAttendantForTransaction(PumpTransaction pumpTransaction, ControllerPtsConfiguration controllerPtsConfiguration) {

        WorkDayShiftPlanningDto workDayShiftPlanningDto = workDayShiftPlanningService.findByStationAndDay(controllerPtsConfiguration.getControllerPts().getStationId(), pumpTransaction.getDateTime().toLocalDate());
        if (Objects.nonNull(workDayShiftPlanningDto)) {
            Optional<WorkDayShiftPlanningExecutionDto> workDayShiftPlanningExecutionDto = workDayShiftPlanningExecutionService.findByWorkDay(workDayShiftPlanningDto.getId());
            if (workDayShiftPlanningExecutionDto.isPresent()) {
                Set<ShiftPlanningExecutionDto> planningExecution = workDayShiftPlanningExecutionDto.get().getShiftPlanningExecutions();
                Optional<ShiftPlanningExecutionDto> filteredPlanningExecution = planningExecution.stream()
                        .filter(shiftPlanningExecutionDto ->
                                Objects.nonNull(shiftPlanningExecutionDto) &&
                                        shiftPlanningExecutionDto.getStartDateTime().isBefore(pumpTransaction.getDateTime()) &&
                                        shiftPlanningExecutionDto.getEndDateTime().isAfter(pumpTransaction.getDateTime()))
                        .findFirst();

                if (filteredPlanningExecution.isPresent()) {
                    ShiftPlanningExecutionDto planningExecutionDto = filteredPlanningExecution.get();
                    List<ShiftPlanningExecutionDetailDto> shiftPlanningExecutionDetailDtos = planningExecutionDto.getShiftPlanningExecutionDetail();

                    Optional<ShiftPlanningExecutionDetailDto> planningExecutionDetailDto = shiftPlanningExecutionDetailDtos.stream()
                            .filter(shiftPlanningExecutionDetailDto ->
                                    shiftPlanningExecutionDetailDto.getPump().getIdConf().equals(pumpTransaction.getPump().getIdConf()) &&
                                            shiftPlanningExecutionDetailDto.getNozzle().getIdConf().equals(pumpTransaction.getNozzle().getIdConf()))
                            .findFirst();

                    if (planningExecutionDetailDto.isPresent()) {
                        PumpAttendantDto pumpAttendantDto = planningExecutionDetailDto.get().getPumpAttendant();
                        PumpAttendant attendant = mapper.toEntity(pumpAttendantDto);
                        // Mapping dto to entity leads to id null --> setId
                        attendant.setId(pumpAttendantDto.getId());
                        // Uninitialized version value 'null' after mapping --> setVersion
                        attendant.setVersion(pumpAttendantDto.getVersion());
                        pumpTransaction.setPumpAttendant(attendant);
                    }
                }
            }
        }
    }
    /**
     * Retrieves a list of sales data for a given controller, chart type, pump, fuel type,
     * and date range. The data type (volume or amount) is determined by the `chartType` parameter.
     *
     * @param idCtr     The ID of the controller (station or other identifier) to filter the data.
     * @param chartType The type of chart data to retrieve ("volume" or "amount").
     * @param pump      The pump identifier to filter the data.
     * @param fuel      The fuel type identifier to filter the data.
     * @param startDate The start date for the data range.
     * @param endDate   The end date for the data range.
     * @return A list of data objects (volume or amount data) based on the specified chart type.
     * @throws IllegalArgumentException if the provided chartType is invalid.
     */
    public <T> List<T> chartOfSalesType(Long idCtr, String chartType, String pump, String fuel,
                                        LocalDateTime startDate, LocalDateTime endDate) {
        if (EnumFilter.VOLUME.toString().equalsIgnoreCase(chartType)) {
            return (List<T>) chartOfSalesVolume(idCtr, pump, fuel, startDate, endDate);
        } else if (EnumFilter.AMOUNT.toString().equalsIgnoreCase(chartType)) {
            return (List<T>) chartOfSalesAmount(idCtr, pump, fuel, startDate, endDate);
        } else {
            throw new IllegalArgumentException("Invalid chartType");
        }
    }
    /**
     * Retrieves a list of sales amount data for a specified controller, pump, and fuel type within a given date range.
     * The period for sales aggregation (daily or monthly) depends on the length of the date range.
     *
     * @param idCtr     The ID of the controller (station or identifier) to filter data.
     * @param startDate The start date of the period for which sales data is required.
     * @param endDate   The end date of the period for which sales data is required.
     * @return A list of objects containing sales amount data aggregated by the specified period.
     * @throws IllegalArgumentException if startDate or endDate is null.
     */
    public List<ChartAllFuelAndAllPumpDto> chartOfSalesAmount(
            Long idCtr,String pump,String fuel, LocalDateTime startDate, LocalDateTime endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(DATE_NOT_NULL);
        }
        if (idCtr == null) {
            throw new IllegalArgumentException("Controller ID must not be null");
        }
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        String unit = daysBetween <= 31 ? "YYYY-MM-DD" : "YYYY-MM";
        // Retrieve current controller configuration
        Optional<ControllerPtsConfiguration> configOpt = controllerPtsConfigurationRepository.findCurrentConfigurationOnController(idCtr);
        if (!configOpt.isPresent()) {
            return Collections.emptyList();
        }
        ControllerPtsConfiguration config = configOpt.get();

        // Get all pump IDs and map them to fuel IDs
        List<Long> pumpIds = pumpService.findPumpIdsByControllerConfiguration(config.getConfigurationId(), config.getPtsId());
        Map<Long, List<Long>> pumpToFuelIdsMap = new HashMap<>();
        for (Long pumpId : pumpIds) {
            List<Long> fuelIdsForPump = nozzleService.findAllFuelIdsByConfigurationAndPumpId(pumpId, config.getConfigurationId(), config.getPtsId());
            pumpToFuelIdsMap.put(pumpId, fuelIdsForPump);
        }
        return processChartAmountSales(idCtr,pump,fuel,startDate,endDate,pumpToFuelIdsMap,unit);
    }

    public List<ChartAllFuelAndAllPumpDto> processChartAmountSales(Long idCtr, String pump, String fuel, LocalDateTime startDate, LocalDateTime endDate, Map<Long, List<Long>> pumpToFuelIdsMap, String unit){
        boolean allPumps = EnumFilter.ALL.toString().equalsIgnoreCase(pump);
        boolean allFuels = EnumFilter.ALL.toString().equalsIgnoreCase(fuel);
        Map<String, Double> salesByMonth = new HashMap<>();
        if (allPumps && allFuels){
            for (Map.Entry<Long, List<Long>> entry : pumpToFuelIdsMap.entrySet()) {
                Long pumpId = entry.getKey();
                List<Long> fuelIds = entry.getValue();

                for (Long fuelId : fuelIds) {
                    // Query the repository for this pump and fuel combination
                    List<Object[]> queryResults = transactionRepository.getAggregatedAmountSales(
                            idCtr, startDate, endDate, pumpId, fuelId, unit);

                    // Process the query results
                    for (Object[] result : queryResults) {
                        String monthUnit = (String) result[3];
                        Double sales = (Double) result[7];

                        // Accumulate sales by month_unit
                        salesByMonth.merge(monthUnit, sales, Double::sum);
                    }
                }
            }

        }else if (!allPumps && allFuels) {
            Long specificPump = Long.parseLong(pump);
            List<Long> fuelIds = pumpToFuelIdsMap.getOrDefault(specificPump, Collections.emptyList());
            for (Long fuelId : fuelIds) {
                // Query the repository for this pump and fuel combination
                List<Object[]> queryResults = transactionRepository.getAggregatedAmountSales(
                        idCtr, startDate, endDate, specificPump, fuelId, unit);
                // Process the query results
                for (Object[] result : queryResults) {
                    String monthUnit = (String) result[3];
                    Double sales = (Double) result[7];
                    // Accumulate sales by month_unit
                    salesByMonth.merge(monthUnit, sales, Double::sum);
                }
            }
        }

        // Convert to DTOs
        return salesByMonth.entrySet().stream()
                .map(entry -> new ChartAllFuelAndAllPumpDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ChartAllFuelAndAllPumpDto::getDate))
                .collect(Collectors.toList());
    }
    /**
     * Generates a sales volume report for a specified controller, pump, and fuel grade over a date range.
     * Adapts the report period (daily, monthly, yearly) based on the date range duration.
     *
     * @param idCtr        The ID of the controller to filter the data.
     * @param pump         The pump identifier (or "all" to include all pumps).
     * @param fuel        The fuel grade identifier (or "all" to include all fuel grades).
     * @param startDate    The start datetime of the date range.
     * @param endDate      The end datetime of the date range.
     * @return A list of sales volume data according to the specified criteria and date range breakdown.
     */
    public List<ChartFuelAllPumpDto> chartOfSalesVolume(
            Long idCtr, String pump, String fuel, LocalDateTime startDate, LocalDateTime endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(DATE_NOT_NULL);
        }
        if (idCtr == null) {
            throw new IllegalArgumentException("Controller ID must not be null");
        }

        // Determine unit for date formatting
        String unit = determineUnit(startDate,endDate);

        // Retrieve the current controller configuration
        Optional<ControllerPtsConfiguration> configOpt = controllerPtsConfigurationRepository.findCurrentConfigurationOnController(idCtr);
        if (!configOpt.isPresent()) {
            return Collections.emptyList();
        }
        ControllerPtsConfiguration config = configOpt.get();

        // Get all pump IDs and map them to fuel IDs
        List<Long> pumpIds = pumpService.findPumpIdsByControllerConfiguration(config.getConfigurationId(), config.getPtsId());
        Map<Long, List<String>> pumpToFuelIdsMap = new HashMap<>();
        for (Long pumpId : pumpIds) {
            List<String> fuelIdsForPump = nozzleService.findAllFuelByConfigurationAndPumpId(pumpId, config.getConfigurationId(), config.getPtsId());
            pumpToFuelIdsMap.put(pumpId, fuelIdsForPump);
        }
        return processChartVolumeSales(idCtr,pump,fuel,startDate,endDate,pumpToFuelIdsMap,unit);
    }

    /**
     * Calculates the total sales volume for a specific fuel grade and pump within a given date range.
     *
     * @param pump    The ID of the pump.
     * @param idCtr     The controller ID.
     * @param fuel The fuel grade (type of fuel).
     * @param startDate The start date of the calculation period.
     * @param endDate   The end date of the calculation period.
     * @return The calculated sales volume for the fuel grade.
     */
    private List<ChartFuelAllPumpDto> processChartVolumeSales(Long idCtr, String pump, String fuel, LocalDateTime startDate, LocalDateTime endDate, Map<Long, List<String>> pumpToFuelIdsMap, String unit){

        boolean allPumps = EnumFilter.ALL.toString().equalsIgnoreCase(pump);
        boolean allFuels = EnumFilter.ALL.toString().equalsIgnoreCase(fuel);
        // Use a map to aggregate results by fuelName and timeUnit
        Map<String, Double> aggregatedSales = new HashMap<>();

        // Process results based on conditions
        if (allPumps && allFuels) {
            for (Map.Entry<Long, List<String>> entry : pumpToFuelIdsMap.entrySet()) {
                Long pumpId = entry.getKey();
                for (String fuelName : entry.getValue()) {
                    List<Object[]> queryResults = transactionRepository.getAggregatedVolumeSales(
                            idCtr, startDate, endDate, pumpId, fuelName, unit);
                    aggregateQueryResults(queryResults, aggregatedSales);
                }
            }
        } else if (!allPumps && allFuels) {
            Long specificPump = Long.parseLong(pump);
            List<String> fuelIds = pumpToFuelIdsMap.getOrDefault(specificPump, Collections.emptyList());
            for (String fuelName : fuelIds) {
                List<Object[]> queryResults = transactionRepository.getAggregatedVolumeSales(
                        idCtr, startDate, endDate, specificPump, fuelName, unit);
                aggregateQueryResults(queryResults, aggregatedSales);
            }
        } else if (allPumps && !allFuels) {
            for (Map.Entry<Long, List<String>> entry : pumpToFuelIdsMap.entrySet()) {
                Long pumpId = entry.getKey();
                List<Object[]> queryResults = transactionRepository.getAggregatedVolumeSales(
                        idCtr, startDate, endDate, pumpId, fuel, unit);
                aggregateQueryResults(queryResults, aggregatedSales);
            }
        } else {
            Long specificPump = Long.parseLong(pump);
            List<Object[]> queryResults = transactionRepository.getAggregatedVolumeSales(
                    idCtr, startDate, endDate, specificPump, fuel, unit);
            aggregateQueryResults(queryResults, aggregatedSales);
        }

        // Convert the aggregated results into DTOs
        return aggregatedSales.entrySet().stream()
                .map(entry -> {
                    String[] keyParts = entry.getKey().split("\\|");
                    String fuelName = keyParts[0];
                    String timeUnit = keyParts[1];
                    return new ChartFuelAllPumpDto(fuelName, timeUnit, entry.getValue());
                })
                .sorted(Comparator
                        .comparing(ChartFuelAllPumpDto::getDateF).reversed()
                        .thenComparing(ChartFuelAllPumpDto::getNameF).reversed())
                .toList();
    }

    public void aggregateQueryResults(List<Object[]> queryResults, Map<String, Double> aggregatedSales) {
        for (Object[] result : queryResults) {
            String fuelName = (String) result[0];
            String timeUnit = (String) result[1];
            Double sales = (Double) result[2];

            // Use fuelName and timeUnit as the key
            String key = fuelName + "|" + timeUnit;

            // Aggregate sales
            aggregatedSales.merge(key, sales, Double::sum);
        }
    }

    private String determineUnit(LocalDateTime startDate, LocalDateTime endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate) <= 31 ? "YYYY-MM-DD" : "YYYY-MM";
    }
    public TransactionDto mapToPumpTransactionDto(PumpTransaction transactions) {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setPump(transactions.getPump().getIdConf());
        try {
            transactionDto.setNozzle(transactions.getNozzle().getIdConf());
        } catch (Exception e) {
            transactionDto.setNozzle(null);
        }
        transactionDto.setDateTimeStart(transactions.getDateTimeStart());
        transactionDto.setTransaction(transactions.getTransactionReference());
        transactionDto.setVolume(BigDecimal.valueOf(transactions.getVolume()));
        transactionDto.setPrice(transactions.getPrice());
        transactionDto.setAmount(BigDecimal.valueOf(transactions.getAmount()));
        Currency currency = transactions.getControllerPtsConfiguration().getControllerPts().getStation().getCountry().getCurrency();
        transactionDto.setDevise(currency.getCode());
        transactionDto.setTotalVolume(transactions.getTotalVolume());
        transactionDto.setTotalAmount(transactions.getTotalAmount());
        transactionDto.setTag(transactions.getTag());
        if (!Objects.isNull(transactions.getPumpAttendant())) {
            String firstName = transactions.getPumpAttendant().getFirstName();
            String lastName = transactions.getPumpAttendant().getLastName();
            String fullName = firstName + " " + lastName;
            transactionDto.setPumpAttendantName(fullName);
        }
        if (transactions.getFuelGrade() != null) {
            transactionDto.setFuelGradeName(transactions.getFuelGrade().getName());
        }
        return (transactionDto);
    }
    /**
     * Retrieves sales data for each pump associated with a specific controller within a given date range.
     *
     * @param idCtr      The ID of the controller to retrieve sales data for.
     * @param startDate  The start date of the sales period.
     * @param endDate    The end date of the sales period.
     * @return A list of SalesDto objects, each containing the sales data for a pump.
     * @throws IllegalArgumentException if startDate or endDate are null.
     */
    public List<SalesDto> getSalesByController(Long idCtr, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(DATE_NOT_NULL);
        }

        Optional<ControllerPtsConfiguration> configOpt = controllerPtsConfigurationRepository.findCurrentConfigurationOnController(idCtr);
        if (!configOpt.isPresent()) {
            return Collections.emptyList();
        }

        ControllerPtsConfiguration config = configOpt.get();
        List<Long> pumpIds = pumpService.findPumpIdsByControllerConfiguration(config.getConfigurationId(), config.getPtsId());

        double totalAllSales = 0.0;
        List<SalesDto> salesData = new ArrayList<>();

        // Iterate through pumps and calculate sales for each fuel type per pump
        for (Long pumpId : pumpIds) {
            List<Long> fuelIds = nozzleService.findAllFuelIdsByConfigurationAndPumpId(pumpId, config.getConfigurationId(), config.getPtsId());

            double pumpSales = fuelIds.stream()
                    .mapToDouble(fuelId -> calculateGradeSales(pumpId, idCtr, fuelId, startDate, endDate))
                    .sum();

            if (pumpSales > 0) {
                totalAllSales += pumpSales;

                SalesDto salesDto = new SalesDto(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        pumpSales,
                        0.0,
                        pumpId
                );
                salesData.add(salesDto);
            }
        }

        // Set totalAllSales in each SalesDto
        for (SalesDto salesDto : salesData) {
            salesDto.setAllSales(totalAllSales);
        }

        return salesData;
    }
    /**
     * Retrieves sales data by fuel grade and pump for the given controller, pump, start date, and end date.
     *
     * @param idCtr    The controller ID.
     * @param pumpId   The pump ID.
     * @param startDate The start date of the period.
     * @param endDate   The end date of the period.
     * @return A list of SalesGradesByPump objects, each representing a fuel grade and its total sales for a specific pump.
     */
    public List<SalesGradesByPump> getSalesByGradesAndPump(Long idCtr, Long pumpId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(DATE_NOT_NULL);
        }

        Optional<ControllerPtsConfiguration> controllerPtsConfiguration = controllerPtsConfigurationRepository.findCurrentConfigurationOnController(idCtr);
        if (controllerPtsConfiguration.isPresent()) {
            String configurationId = controllerPtsConfiguration.get().getConfigurationId();
            List<SalesGradesByPump> salesDataByGrade = new ArrayList<>();
            List<Long> fuelIds = nozzleService.findAllFuelIdsByConfigurationAndPumpId(pumpId,configurationId, controllerPtsConfiguration.get().getPtsId());
            for (Long fuelId : fuelIds) {
                double gradeSales = calculateGradeSales(pumpId, idCtr, fuelId, startDate, endDate);

                if (gradeSales > 0) {
                    FuelGrade fuelGrade = fuelGradesService.findFuelGradeByIdConfAndController(fuelId, controllerPtsConfiguration.get().getConfigurationId(), idCtr);
                    SalesGradesByPump salesGrade = new SalesGradesByPump(pumpId, fuelGrade.getName(), gradeSales);
                    salesDataByGrade.add(salesGrade);
                }
            }
            return salesDataByGrade;
        }
        return null;
    }

    /**
     * Calculates the sales for a specific fuel grade and pump within a given date range.
     *
     * @param pumpId    The ID of the pump.
     * @param idCtr     The controller ID.
     * @param fuelId    The fuel ID for the grade.
     * @param startDate The start date of the calculation period.
     * @param endDate   The end date of the calculation period.
     * @return The calculated sales amount for the fuel grade.
     */
    public double calculateGradeSales(Long pumpId, Long idCtr, Long fuelId, LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal endAmount = transactionRepository.findTotalAmountEndForGradeAndPump(pumpId, idCtr, fuelId, startDate, endDate);
            FirstIndexSales firstIndexAmount = transactionRepository.findTotalAmountStartForGradeAndPumpByDate(pumpId, idCtr, fuelId, startDate, endDate);
            BigDecimal startAmount = BigDecimal.ZERO;
            if (firstIndexAmount != null) {
                 startAmount = firstIndexAmount.getTotalIndexStart().subtract(BigDecimal.valueOf(firstIndexAmount.getQuantity()));
            }

        // Skip if endAmount is null or zero
        if (endAmount == null || endAmount.doubleValue() == 0) {
            return 0.0;
        }

        // Calculate sales for the fuel grade
        double fuelGradeSales = endAmount.subtract(startAmount).doubleValue();

        // Apply fallback if fuelGradeSales is zero
        if (fuelGradeSales == 0) {
            fuelGradeSales = transactionRepository.findLastAmountForGradeAndPump(pumpId, idCtr, fuelId, startDate, endDate);
        }

        return fuelGradeSales;
    }

    public Page<PumpTransaction> findTransactionsByFilter(
            Long idCtr,
            TransactionFilterDto filterDto,
            int page, int size
    ) {
        return transactionRepository.findByCriteria(idCtr, filterDto, page, size);
    }

    public byte[] generateExcelTransactionsByFilter(
            Long idCtr,
            TransactionFilterDto filterDto,
            int page, int size,
            List<String> columnsToDisplay,String locale,String filterSummary
    ) throws IOException
    {
        final List<TransactionDto> list = transactionRepository.findByCriteria(idCtr, filterDto, page, size).map(this::mapToPumpTransactionDto).stream().toList();
        return TransactionExcelGenerator.generateExcel(list, columnsToDisplay,locale,filterSummary);
    }
    public byte[] generatePDFTransactionsByFilter(Long idCtr, TransactionFilterDto filterDto, int page, int size, List<String> columnsToDisplay,String locale,String filterSummary) {
        List<TransactionDto> list = transactionRepository.findByCriteria(idCtr, filterDto, page, size)
                .map(this::mapToPumpTransactionDto)
                .stream()
                .toList();
        return TransactionPDFGenerator.generatePDF(list, columnsToDisplay, locale,filterSummary);
    }

    public Optional<PumpTransaction> findFirstTransactionOnDate(String ptsId, Long nozzleId, Long pumpId, LocalDateTime dateTime) {
        return transactionRepository.findFirstTransactionOnDate(ptsId, nozzleId, pumpId, dateTime);
    }

    public Optional<PumpTransaction> findFirstTransactionOnDateByTag(String ptsId, Long nozzleId, Long pumpId, String tag, LocalDateTime dateTime) {
        return transactionRepository.findFirstTransactionOnDateByTag(ptsId, nozzleId, pumpId, tag, dateTime);
    }

    public Optional<PumpTransaction> findLastTransactionOnDate(String ptsId, Long nozzleId, Long pumpId, LocalDateTime dateTime) {
        return transactionRepository.findLastTransactionOnDate(ptsId, nozzleId, pumpId, dateTime);
    }

    public Optional<PumpTransaction> findLastTransactionOnDateByTag(String ptsId, Long nozzleId, Long pumpId, String tag, LocalDateTime dateTime) {
        return transactionRepository.findLastTransactionOnDateByTag(ptsId, nozzleId, pumpId, tag, dateTime);
    }
    public FuelDataIndexStart findInitialIndex(Long pumpId, String ptsId, Long fuelIdConf, LocalDateTime startDate) {
        return transactionRepository.findInitialIndex(pumpId, ptsId, fuelIdConf, startDate);
    }
}
