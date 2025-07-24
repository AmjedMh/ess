package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.model.movements.TankMeasurement;
import com.teknokote.ess.core.repository.TankRepository;
import com.teknokote.ess.core.repository.tank_measurement.TankMeasurementRepository;
import com.teknokote.ess.core.service.cache.MeasurementMapper;
import com.teknokote.ess.core.service.cache.TankMeasurementCache;
import com.teknokote.ess.core.service.impl.tank.TankMeasurementExcelGenerator;
import com.teknokote.ess.core.service.impl.tank.TankMeasurementPDFGenerator;
import com.teknokote.ess.dto.TankFilterDto;
import com.teknokote.ess.dto.TankMeasurementsDto;
import com.teknokote.ess.dto.charts.ReportTankMeasurementAndLevelChartDto;
import com.teknokote.ess.dto.charts.TankLevelPerSalesChartDto;
import com.teknokote.ess.dto.charts.TankMeasurementChartDto;
import com.teknokote.pts.client.upload.dto.MeasurementDto;
import com.teknokote.pts.client.upload.dto.UploadMeasurementDto;
import com.teknokote.pts.client.upload.dto.UploadSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class TankMeasurementServices {
    @Autowired
    private TankMeasurementRepository tankMeasurementRepository;
    @Autowired
    private TankDeliveryService tankDeliveryService;
    @Autowired
    private MeasurementMapper measurementMapper;
    @Autowired
    private TankRepository tankRepository;
    @Value("${app.measurement.interval}")
    private Duration measurementPeriod;
    private final TankMeasurementCache tankMeasurementCache=new TankMeasurementCache();

    public TankMeasurement save(TankMeasurement tankMeasurement){
        return tankMeasurementRepository.save(tankMeasurement);
    }

    /**
     * Retrieves the last cached measurement data for a given controller and tank ID,
     * If not found, fetches it from the database.
     *
     * @param ptsId The controller's PTS ID
     * @param tankId          The tank ID
     * @return The last measurement data as a MeasurementDto
     */
    public MeasurementDto getLastMeasurementData(String ptsId, Long tankId) {
        Function<? super Long, ? extends MeasurementDto> loadFromDBFn = k ->
                getLastTankMeasurement(ptsId, tankId)
                        .map(this::mapToMeasurementDto)
                        .orElse(new MeasurementDto());

        return tankMeasurementCache.getLastMeasurementData(ptsId, tankId, loadFromDBFn);
    }
    public MeasurementDto getDelayedMeasurement(String ptsId, Long tankId,LocalDateTime dateTime) {
        Function<? super Long, ? extends MeasurementDto> loadFromDBFn = k ->
                getLastTankMeasurementByDate(ptsId, tankId,dateTime)
                        .map(this::mapToMeasurementDto)
                        .orElse(new MeasurementDto());

        return tankMeasurementCache.getLastDelayedMeasurementData(ptsId, tankId, loadFromDBFn);
    }

    /**
     * Returns all tanks measurements
     * @param ptsId
     * @return
     */
    public Map<Long, MeasurementDto> getAllTanksMeasurements(String ptsId) {
        return tankMeasurementCache.getTankMeasurementCache(ptsId);
    }

    /**
     * Returns sorted tank measurments by tankIdConf
     * @param ptsId
     * @return
     */
    public List<MeasurementDto> getAllTankMeasurementsSortedByTank(String ptsId) {
        Map<Long, MeasurementDto> cachedMeasurements = getAllTanksMeasurements(ptsId);
        // Check if the cache for the given ptsId is empty
        if (cachedMeasurements == null || cachedMeasurements.isEmpty()) {
            return Collections.emptyList();
        }
        // Sort the values by tank
        return cachedMeasurements.values().stream().sorted(Comparator.comparing(MeasurementDto::getTank)).toList();
    }

    public Optional<TankMeasurement> findById(Long id) {
        return tankMeasurementRepository.findById(id);
    }

    public byte[] generateExcelMeasurementByFilter(
            Long idCtr,
            TankFilterDto filterDto,
            int page, int size,
            List<String> columnsToDisplay,String locale,String filterSummary
    ) throws IOException
    {
        final List<TankMeasurementsDto> list = tankMeasurementRepository.findMeasurementByFilterAndIdController(idCtr, filterDto, page, size).map(this::mapTankMeasurementToDto).stream().toList();
        return TankMeasurementExcelGenerator.generateMeasurementExcel(list, columnsToDisplay,locale,filterSummary);
    }

    public byte[] generatePDFTankMeasurementByFilter(Long idCtr, TankFilterDto filterDto, int page, int size, List<String> columnsToDisplay, String locale, String filterSummary){
        List<TankMeasurementsDto> list = tankMeasurementRepository.findMeasurementByFilterAndIdController(idCtr, filterDto, page, size)
                .map(this::mapTankMeasurementToDto)
                .stream()
                .toList();
        return TankMeasurementPDFGenerator.generateMeasurementPDF(list, columnsToDisplay, locale,filterSummary);
    }
    public Page<TankMeasurement> findMeasurementByFilter(Long idCtr, TankFilterDto filterDto , int page, int size) {
        return tankMeasurementRepository.findMeasurementByFilterAndIdController(idCtr, filterDto, page,size);
    }

    public List<TankMeasurementChartDto> reportTankMeasurementsByTankChart(Long idCtr, String tank, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new ServiceValidationException("startDate and endDate must not be null");
        }
        return tankMeasurementRepository.findAllByControllerPtsIdAndTankIdAndPeriod(idCtr, Long.valueOf(tank), startDate, endDate);
    }

    /**
     * Create and populate a new TankMeasurement record with data from a Measurement object
     * ==> Updates the cache
     * @param tank
     * @param controllerPtsConfiguration
     * @return
     */
    public void addNewMeasurement(Tank tank, ControllerPtsConfiguration controllerPtsConfiguration, MeasurementDto receivedMeasurement, MeasurementDto previousMeasurement) {
        if (receivedMeasurement.isOffline() || receivedMeasurement.getProductVolume() != null && receivedMeasurement.getProductVolume().equals(previousMeasurement.getProductVolume()))return;
        TankMeasurement newMeasurement = measurementMapper.toTankMeasurement(receivedMeasurement,tank,controllerPtsConfiguration);
        tankMeasurementRepository.save(newMeasurement);
    }

    /**
     * Processes an uploaded tank measurement.
     * Uploaded measurements can provide from UploadStatus or UploadReportTankMeasurement
     * @param uploadMeasurementDto
     * @param controllerPtsConfiguration
     */
    public void processUploadedTankMeasurement(UploadMeasurementDto uploadMeasurementDto, ControllerPtsConfiguration controllerPtsConfiguration) {
        if (uploadMeasurementDto.hasMeasurement()) {
            UploadSource uploadSource = uploadMeasurementDto.getUploadSource();
            uploadMeasurementDto.getMeasurements().forEach((tankIdConf, receivedMeasurement) -> {
                LocalDateTime measurementTime = receivedMeasurement.getDateTime();
                // Get latest real-time measurement
                MeasurementDto latestRealTimeMeasurement = getLastMeasurementData(controllerPtsConfiguration.getPtsId(), tankIdConf);
                // Get previous measurement based on whether it's delayed or real-time
                MeasurementDto previousMeasurement = uploadSource.equals(UploadSource.UploadTankMeasurement)
                        ? getDelayedMeasurement(controllerPtsConfiguration.getPtsId(), tankIdConf,measurementTime)
                        : latestRealTimeMeasurement;
                if (shouldSkipMeasurement(previousMeasurement, receivedMeasurement)) return;
                Tank tank = tankRepository.findAllByIdConfAndControllerPtsConfiguration(receivedMeasurement.getTank(), controllerPtsConfiguration);
                addNewMeasurement(tank, controllerPtsConfiguration, receivedMeasurement, previousMeasurement);
                receivedMeasurement.setFuelGrade(tank.getGrade().getName());
                if (uploadSource.equals(UploadSource.UploadTankMeasurement)) {
                    tankMeasurementCache.updateDelayedMeasurementCache(controllerPtsConfiguration.getPtsId(), receivedMeasurement);
                } else {
                    tankMeasurementCache.updateMeasurementCache(controllerPtsConfiguration.getPtsId(), receivedMeasurement);
                }
                tankDeliveryService.monitorDepotage(tank, receivedMeasurement, previousMeasurement, controllerPtsConfiguration,uploadSource);
            });
        }
    }
    /**
     * Skips measurements based on the previous one.
     * skip only when timeSinceLastMeasurement < measurementPeriod
     */
    public boolean shouldSkipMeasurement (MeasurementDto lastMeasurementData, MeasurementDto receivedMeasurement){
        if (lastMeasurementData == null || lastMeasurementData.getProductVolume() == null || lastMeasurementData.getDateTime() == null || receivedMeasurement == null) {
            return false;
        }
        LocalDateTime lastDateTime = lastMeasurementData.getDateTime();
        LocalDateTime receivedDateTime = receivedMeasurement.getDateTime();
        Duration timeSinceLastMeasurement = Duration.between(lastDateTime, receivedDateTime);
        return timeSinceLastMeasurement.compareTo(measurementPeriod) < 0 ;
    }
    /**
     * Retrieves the last ReportTankMeasurement for a given ptsId and tankIdConf.
     *
     * @param controllerPtsId The controller points ID.
     * @param tankIdConf The tank configuration ID.
     * @return An Optional containing the last ReportTankMeasurement, if present.
     */
    public Optional<TankMeasurement> getLastTankMeasurement(String controllerPtsId, Long tankIdConf) {
        return tankMeasurementRepository.getLastMeasurement(controllerPtsId, tankIdConf);
    }

    public Optional<TankMeasurement> getLastTankMeasurementByDate(String controllerPtsId, Long tankIdConf,LocalDateTime dateTime) {
        return tankMeasurementRepository.getLastMeasurementByDate(controllerPtsId, tankIdConf,dateTime);
    }

    // Map TankMeasurement to TankMeasurementsDto with calculated filling percentage
    public TankMeasurementsDto mapTankMeasurementToDto(TankMeasurement tankMeasurement) {

        return measurementMapper.toDto(tankMeasurement);
    }

    // Convert TankMeasurement to a MeasurementDto with calculated values and metrics
    public MeasurementDto mapToMeasurementDto(TankMeasurement tankMeasurement) {
        if (tankMeasurement == null) {
            return null;
        }
        return measurementMapper.mapToMeasurementDto(tankMeasurement);
    }

    // Align and fill missing data between tank measurements and sales levels
    public List<ReportTankMeasurementAndLevelChartDto> alignAndFillMissingData(
            List<TankMeasurementChartDto> measurementData,
            List<TankLevelPerSalesChartDto> levelData) {

        List<ReportTankMeasurementAndLevelChartDto> alignedData = new ArrayList<>();
        int measurementIndex = 0;
        int levelIndex = 0;

        TankMeasurementChartDto lastMeasurement = null;
        TankLevelPerSalesChartDto lastLevel = null;

        // Traverse both lists with two pointers
        while (measurementIndex < measurementData.size() || levelIndex < levelData.size()) {
            LocalDateTime currentDate;

            // Get the next dates from each list
            LocalDateTime nextMeasurementTime = measurementIndex < measurementData.size()
                    ? measurementData.get(measurementIndex).getDateTime()
                    : null;
            LocalDateTime nextLevelTime = levelIndex < levelData.size()
                    ? levelData.get(levelIndex).getDateTime()
                    : null;

            // Determine the date to align on
            if (nextMeasurementTime != null && (nextLevelTime == null || !nextMeasurementTime.isAfter(nextLevelTime))) {
                currentDate = nextMeasurementTime;
                lastMeasurement = measurementData.get(measurementIndex++);
            } else {
                currentDate = nextLevelTime;
                lastLevel = levelData.get(levelIndex++);
            }

            ReportTankMeasurementAndLevelChartDto alignedPoint = new ReportTankMeasurementAndLevelChartDto(
                    currentDate,
                    lastMeasurement != null ? lastMeasurement.getTank() : null,
                    lastMeasurement != null ? lastMeasurement.getProductVolume() : null,
                    lastLevel != null ? lastLevel.getProductVolume() : null
            );
            alignedData.add(alignedPoint);
        }

        return alignedData;
    }

}
