package com.teknokote.ess.core.service.impl;

import com.teknokote.core.model.AuditableEntity;
import com.teknokote.ess.core.model.EnumDeliveryStatus;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.model.movements.TankDelivery;
import com.teknokote.ess.core.model.movements.TankLevelPerSales;
import com.teknokote.ess.core.repository.tank_delivery.TankDeliveryRepository;
import com.teknokote.ess.core.repository.transactions.TransactionRepository;
import com.teknokote.ess.core.service.cache.DepotageMonitoringCache;
import com.teknokote.ess.core.service.cache.MeasurementTracking;
import com.teknokote.ess.core.service.cache.TankDeliveryMapper;
import com.teknokote.ess.core.service.impl.tank.TankDeliveryExcelGenerator;
import com.teknokote.ess.core.service.impl.tank.TankDeliveryPDFGenerator;
import com.teknokote.ess.core.service.impl.transactions.TankLevelPerSalesService;
import com.teknokote.ess.dto.TankDeliveryDto;
import com.teknokote.ess.dto.TankFilterDto;
import com.teknokote.ess.utils.EssUtils;
import com.teknokote.pts.client.upload.dto.MeasurementDto;
import com.teknokote.pts.client.upload.dto.UploadSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TankDeliveryService {

    public static final String ALL = "all";
    // Configurable volume fluctuation threshold and measurement interval from application properties
    @Value("${app.delivery.volume-fluctuation}")
    private Double volumeFluctuation;
    @Value("${app.delivery.delivery-start-confirmation}")
    private int deliveryStartConfirmation;
    @Value("${app.delivery.delivery-end-confirmation}")
    private int deliveryEndConfirmation;
    @Autowired
    private TankDeliveryRepository tankDeliveryRepository;
    @Autowired
    private TankLevelPerSalesService tankLevelPerSalesService;
    @Autowired
    private TankDeliveryMapper tankDeliveryMapper;
    //dependance circulaire
    @Autowired
    private TransactionRepository transactionRepository;

    private final DepotageMonitoringCache depotageMonitoringCache=new DepotageMonitoringCache();

    public List<TankDelivery> getAll() {
        return tankDeliveryRepository.findAll();
    }

    /**
     * Finds TankDelivery entries based on filter type and pagination.
     *
     * @param idCtr The controller ID.
     * @param page The page number for pagination.
     * @param size The page size for pagination.
     * @return A Page of filtered TankDelivery entities.
     */
    public Page<TankDelivery> findDeliveryByFilter(Long idCtr, TankFilterDto filterDto , int page, int size) {
        return tankDeliveryRepository.findTankDeliveryByIdController(idCtr, filterDto, page,size);
    }
    public byte[] generateExcelDeliveryByFilter(
            Long idCtr,
            TankFilterDto filterDto,
            int page, int size,
            List<String> columnsToDisplay,String locale,String filterSummary
    ) throws IOException
    {
        final List<TankDeliveryDto> list = tankDeliveryRepository.findTankDeliveryByIdController(idCtr, filterDto, page, size).map(this::mapDeliveryToDto).stream().toList();
        return TankDeliveryExcelGenerator.generateDeliveryExcel(list, columnsToDisplay,locale,filterSummary);
    }

    public byte[] generatePDFTankDeliveryByFilter(Long idCtr, TankFilterDto filterDto, int page, int size, List<String> columnsToDisplay, String locale, String filterSummary) {
        List<TankDeliveryDto> list = tankDeliveryRepository.findTankDeliveryByIdController(idCtr, filterDto, page, size)
                .map(this::mapDeliveryToDto)
                .stream()
                .toList();
        return TankDeliveryPDFGenerator.generateDeliveryPDF(list, columnsToDisplay, locale,filterSummary);
    }

    public TankDelivery findLatestTankDeliveryByTankId(Long idCtr, Long tank) {
        return tankDeliveryRepository.findLastDeliveriesByTankIdConf(idCtr, tank);
    }

    /**
     * Maps a TankDelivery entity to a TankDeliveryDto for data transfer.
     *
     * @param tankDelivery The TankDelivery entity to map.
     * @return A TankDeliveryDto containing mapped data, or null if tankDelivery is null.
     */
    public TankDeliveryDto mapDeliveryToDto(TankDelivery tankDelivery) {
        if (tankDelivery != null) {
            return  tankDeliveryMapper.toDto(tankDelivery);
        }
        return null;
    }

    /**
     * Creates a new TankDelivery entity based on a measurement and previous data.
     *
     * @param tankMeasurement The current measurement data.
     * @param previousMeasurement The previous measurement data.
     * @param tank The tank for which the delivery is created.
     * @param controllerPtsConfiguration The controller points configuration.
     * @param dateTime The end datetime of the delivery.
     * @return The newly created TankDelivery entity.
     */
    public TankDelivery createNewTankDelivery(MeasurementDto tankMeasurement,
                                              MeasurementDto previousMeasurement,
                                              Tank tank,
                                              ControllerPtsConfiguration controllerPtsConfiguration,
                                              LocalDateTime dateTime,
                                              EnumDeliveryStatus status,
                                              UploadSource uploadSource) {

        TankDelivery newDelivery = tankDeliveryMapper.measurementToDelivery(tankMeasurement,previousMeasurement,tank,controllerPtsConfiguration,dateTime,status);
        // Calculate unloading duration (duree de depotage)
        Duration dureeDepotage = Duration.between(newDelivery.getStartDateTime(), newDelivery.getEndDateTime());
        String formattedDuration = EssUtils.formattedDuration(dureeDepotage);
        newDelivery.setDuration(formattedDuration);
        newDelivery.setUploadSource(uploadSource);
        newDelivery = tankDeliveryRepository.save(newDelivery);
        // Create new TankLevelPerSales record after saving delivery
        if (uploadSource.equals(UploadSource.UploadStatus)){
            createNewTankLevelPerSales(newDelivery, controllerPtsConfiguration);
        }
        return newDelivery;
    }

    /**
     * Updates an existing TankDelivery with new measurement data.
     *
     * @param existingDelivery The TankDelivery entity to update.
     * @param currentStatusMeasurement The current measurement data.
     * @param controllerPtsConfiguration The controller points configuration.
     * @param dateTime The new end datetime of the delivery.
     * @return The updated TankDelivery entity.
     */
    TankDelivery updateTankDelivery(TankDelivery existingDelivery,
                                    MeasurementDto currentStatusMeasurement,
                                    ControllerPtsConfiguration controllerPtsConfiguration,
                                    LocalDateTime dateTime,
                                    UploadSource uploadSource) {

        BigDecimal additionalHeight = BigDecimal.valueOf(currentStatusMeasurement.getProductHeight()).add(existingDelivery.getStartProductHeight());
        existingDelivery.setProductVolume(currentStatusMeasurement.getProductVolume() - existingDelivery.getStartProductVolume());
        existingDelivery.setProductHeight(additionalHeight);
        existingDelivery.setEndProductVolume(currentStatusMeasurement.getProductVolume());
        existingDelivery.setEndProductHeight(BigDecimal.valueOf(currentStatusMeasurement.getProductHeight()));
        existingDelivery.setEndDateTime(dateTime);

        // Calculating dure de depotage (duration of unloading)
        Duration dureeDepotage = Duration.between(existingDelivery.getStartDateTime(), existingDelivery.getEndDateTime());
        String formattedDuration = EssUtils.formattedDuration(dureeDepotage);
        existingDelivery.setDuration(formattedDuration);
        existingDelivery = tankDeliveryRepository.save(existingDelivery);

        // Call the createNewTankLevelPerSales method to handle TankLevelPerSales creation
        if (uploadSource.equals(uploadSource.UploadStatus)){
            createNewTankLevelPerSales(existingDelivery, controllerPtsConfiguration);
        }
        return existingDelivery;
    }
    TankDelivery finalizeTankDelivery(TankDelivery lastDelivery, MeasurementDto firstStableMeasurement, ControllerPtsConfiguration controllerPtsConfiguration, Double salesVolume,UploadSource uploadSource) {
        log.info("transactions volume on ongoing delivery  : {}",salesVolume);
        lastDelivery.setStatus(EnumDeliveryStatus.FINISH);
        lastDelivery.setSalesVolume(salesVolume);
        lastDelivery.setEndProductVolume(firstStableMeasurement.getProductVolume());
        lastDelivery.setEndProductHeight(BigDecimal.valueOf(firstStableMeasurement.getProductHeight()));
        lastDelivery.setProductHeight(lastDelivery.getEndProductHeight().subtract(lastDelivery.getStartProductHeight()));
        lastDelivery.setProductVolume(lastDelivery.getEndProductVolume() - lastDelivery.getStartProductVolume() + ((salesVolume != null) ? salesVolume :0.0));
        lastDelivery.setEndDateTime(firstStableMeasurement.getDateTime());
        Duration dureeDepotage = Duration.between(lastDelivery.getStartDateTime(), lastDelivery.getEndDateTime());
        String formattedDuration = EssUtils.formattedDuration(dureeDepotage);
        lastDelivery.setDuration(formattedDuration);
        lastDelivery = tankDeliveryRepository.save(lastDelivery);
        log.info("Depotage ended for PTS: {}, Tank: {} with delivered volume : {}", controllerPtsConfiguration.getPtsId(), lastDelivery.getTank().getIdConf(),lastDelivery.getProductVolume());
        // Call the createNewTankLevelPerSales method to handle TankLevelPerSales creation
        if (uploadSource.equals(UploadSource.UploadStatus)) {
            createNewTankLevelPerSales(lastDelivery, controllerPtsConfiguration);
        }
        return lastDelivery;
    }
    // create new tank level from delivery
    void createNewTankLevelPerSales(TankDelivery tankDelivery, ControllerPtsConfiguration controllerPtsConfiguration) {

        // Create the new TankLevelPerSales entry
        TankLevelPerSales tankLevelPerSales = new TankLevelPerSales();
        tankLevelPerSales.setTank(tankDelivery.getTank().getIdConf());
        tankLevelPerSales.setFuelGrade(tankDelivery.getTank().getGrade().getName());
        tankLevelPerSales.setTankVolumeChanges(tankDelivery.getEndProductVolume());
        tankLevelPerSales.setDateTime(tankDelivery.getEndDateTime());
        tankLevelPerSales.setControllerPts(controllerPtsConfiguration.getControllerPts());
        tankLevelPerSales.setControllerPtsConfiguration(controllerPtsConfiguration);

        // Save the new TankLevelPerSales entry
        tankLevelPerSalesService.save(tankLevelPerSales);

    }
    TankDelivery getLastDelivery(String controllerPtsId, Long tankId, LocalDateTime dateTime) {
            // Fetch the last measurement from the database
            return tankDeliveryRepository.findLastDeliveryByTankIdConfAndPtsId(controllerPtsId, tankId,dateTime).orElse(null);
    }
    private TankDelivery findNextDeliveryByTankIdConfAndPtsId(String controllerPtsId, Long tankId,LocalDateTime dateTime) {
        return tankDeliveryRepository.findNextDeliveryByTankIdConfAndPtsId(controllerPtsId, tankId,dateTime).orElse(null);
    }

    /**
     * Monitors the depotage process for a given tank and updates the delivery status based on measurements.
     *
     * @param tank                      The tank being monitored.
     * @param newMeasurement            The latest measurement from the tank.
     * @param previousMeasurement       The previous measurement to compare for changes in volume.
     * @param controllerPtsConfiguration The configuration of the controller managing the tank.
     */
    public void monitorDepotage(Tank tank, MeasurementDto newMeasurement, MeasurementDto previousMeasurement, ControllerPtsConfiguration controllerPtsConfiguration, UploadSource uploadSource) {
        if (previousMeasurement == null || previousMeasurement.getProductVolume() == null || newMeasurement.isOffline()) return;
        String ptsId=controllerPtsConfiguration.getPtsId();
        Long tankId = tank.getIdConf();
        Double previousVolume = previousMeasurement.getProductVolume();
        Double currentVolume = newMeasurement.getProductVolume();
        // Get monitoring data.
        MeasurementTracking measurementTracking = depotageMonitoringCache.getOrCreateMeasurementTracking(ptsId,tankId,uploadSource);
        measurementTracking.addInitialMeasurement(previousMeasurement);
        TankDelivery lastDelivery = getLastDelivery(ptsId, tankId,newMeasurement.getDateTime());
        measurementTracking.updateMeasurementTracking(newMeasurement, previousVolume,volumeFluctuation);
        // Start monitoring if volume increases significantly.
        if (currentVolume > previousVolume && (currentVolume - previousVolume > volumeFluctuation)) {
            log.info("Depotage detection started for PTS: {}, Tank: {}, Measurements: {}", ptsId, tankId, measurementTracking.getMeasurements().size());
            if (lastDelivery != null && EnumDeliveryStatus.IN_PROGRESS.equals(lastDelivery.getStatus())) {
                // Update ongoing delivery if already in progress.
                updateDepotage(measurementTracking.getMeasurements(), controllerPtsConfiguration, lastDelivery,uploadSource);
            } else {
                // Confirm and create a new delivery
                confirmDepotage(ptsId, tank, measurementTracking, controllerPtsConfiguration, uploadSource,lastDelivery);
            }
        } else {
            if(lastDelivery != null && EnumDeliveryStatus.IN_PROGRESS.equals(lastDelivery.getStatus())) {
                finalizeDepotage(ptsId, tankId, measurementTracking, controllerPtsConfiguration, lastDelivery,uploadSource);
            }
        }
    }

    /**
     * Confirms depotage based on increasing measurements and creates a new delivery if applicable.
     *
     * @param ptsId                     The identifier for the PTS (controller).
     * @param tank                      The tank being monitored.
     * @param measurementTracking       object for tracking measurement data
     * @param controllerPtsConfiguration The configuration of the controller managing the tank.
     */
    public void confirmDepotage(String ptsId, Tank tank,MeasurementTracking measurementTracking, ControllerPtsConfiguration controllerPtsConfiguration,UploadSource uploadSource,TankDelivery lastDelivery) {
        if (measurementTracking == null) return; // No monitoring data exists.

        int stableOrIncreasingCount = measurementTracking.getIncreasingCount();
        List<MeasurementDto> measurements = measurementTracking.getMeasurements();

        if (stableOrIncreasingCount >= deliveryStartConfirmation) {
            MeasurementDto firstStableMeasurement = measurements.get(measurements.size() - stableOrIncreasingCount - 1);
            MeasurementDto lastMeasurement = measurements.get(measurements.size() - 1);
            Double totalVolumeAdded = lastMeasurement.getProductVolume() - firstStableMeasurement.getProductVolume();

            log.info("Depotage confirmed for PTS: {}, Tank: {}, Total Volume Added: {}", ptsId, tank.getIdConf(), totalVolumeAdded);
            // Create a new TankDelivery.
            if (lastDelivery == null || lastDelivery != null && Duration.between(lastDelivery.getStartDateTime(), firstStableMeasurement.getDateTime()).toMinutes() > 10) {
                createNewTankDelivery(lastMeasurement, firstStableMeasurement, tank, controllerPtsConfiguration, lastMeasurement.getDateTime(), EnumDeliveryStatus.IN_PROGRESS, uploadSource);
            }
        }
    }

    /**
     * Updates an ongoing depotage by recalculating the total volume added.
     * @param monitoringList            The list of measurements for the current depotage.
     * @param controllerPtsConfiguration The configuration of the controller managing the tank.
     */
    private void updateDepotage(List<MeasurementDto> monitoringList, ControllerPtsConfiguration controllerPtsConfiguration,TankDelivery lastDelivery,UploadSource uploadSource) {
        MeasurementDto lastMeasurement = monitoringList.get(monitoringList.size() - 1);
        if (uploadSource.equals(UploadSource.UploadStatus)){
            Duration duration = Duration.between(lastDelivery.getStartDateTime(), lastMeasurement.getDateTime());
            if (lastDelivery.getUploadSource().equals(UploadSource.UploadTankMeasurement)){
                /**delivery is delayed and we receive realTime measurement on the range of delivery duration==> update delivery with realtime**/
                if (duration.toMinutes() <= 30) {
                    updateTankDelivery(lastDelivery, lastMeasurement, controllerPtsConfiguration, lastMeasurement.getDateTime(),uploadSource);
                }
            }
            else{
                updateTankDelivery(lastDelivery, lastMeasurement, controllerPtsConfiguration, lastMeasurement.getDateTime(),uploadSource);
            }
        }else{
            if(lastDelivery.getUploadSource().equals(UploadSource.UploadTankMeasurement)) {
                updateTankDelivery(lastDelivery, lastMeasurement, controllerPtsConfiguration, lastMeasurement.getDateTime(),uploadSource);
            }else {
                if (lastMeasurement.getDateTime().isAfter(lastDelivery.getEndDateTime())){
                    updateTankDelivery(lastDelivery, lastMeasurement, controllerPtsConfiguration, lastMeasurement.getDateTime(),uploadSource);
                }
            }
        }
    }

    /**
     * Finalizes the depotage if stable or decreasing measurements are detected.
     *
     * @param ptsId                     The identifier for the PTS (controller).
     * @param tankId                    The tank identifier.
     * @param measurementTracking       object for tracking measurement data
     * @param controllerPtsConfiguration The configuration of the controller managing the tank.
     * @return                          The finalized TankDelivery, if applicable.
     */
    private void finalizeDepotage(String ptsId, Long tankId,MeasurementTracking measurementTracking, ControllerPtsConfiguration controllerPtsConfiguration, TankDelivery lastDelivery,UploadSource uploadSource) {
        if (measurementTracking == null || lastDelivery == null || !EnumDeliveryStatus.IN_PROGRESS.equals(lastDelivery.getStatus())) {
            return; // No monitoring data or no ongoing delivery.
        }

        int stableOrDecreasingCount = measurementTracking.getStableOrDecreasingCount();
        List<MeasurementDto> measurements = measurementTracking.getMeasurements();
        TankDelivery finishedDelivery;
        if (stableOrDecreasingCount >= deliveryEndConfirmation) {
            MeasurementDto firstStableMeasurement = measurements.get(measurements.size() - stableOrDecreasingCount - 1);
            MeasurementDto lastMeasurement = measurements.get(measurements.size() - 1);

            // Update TankDelivery details.
            Double salesVolumeOnOngoingDelivery = transactionRepository.findSalesVolumeByDateAndTank(ptsId,tankId,lastDelivery.getStartDateTime(),firstStableMeasurement.getDateTime());
            if (uploadSource.equals(UploadSource.UploadStatus)){
                    Duration duration = Duration.between(lastDelivery.getStartDateTime(), lastMeasurement.getDateTime());
                    if(lastDelivery.getUploadSource().equals(UploadSource.UploadTankMeasurement)){
                        if (duration.toMinutes() <=30) {
                            finalizeTankDelivery(lastDelivery, firstStableMeasurement, controllerPtsConfiguration,salesVolumeOnOngoingDelivery,uploadSource);
                        }
                    }else{
                        finalizeTankDelivery(lastDelivery, firstStableMeasurement, controllerPtsConfiguration,salesVolumeOnOngoingDelivery,uploadSource);
                    }
            }else{
                if(lastDelivery.getUploadSource().equals(UploadSource.UploadTankMeasurement)) {
                    finishedDelivery = finalizeTankDelivery(lastDelivery, firstStableMeasurement, controllerPtsConfiguration,salesVolumeOnOngoingDelivery,uploadSource);
                    recalculateTankLevelPerSalesOnDelayedDelivery(finishedDelivery, controllerPtsConfiguration);
                }else {
                    if (lastMeasurement.getDateTime().isAfter(lastDelivery.getEndDateTime())){
                        finishedDelivery = finalizeTankDelivery(lastDelivery, firstStableMeasurement, controllerPtsConfiguration,salesVolumeOnOngoingDelivery,uploadSource);
                        recalculateTankLevelPerSalesOnDelayedDelivery(finishedDelivery, controllerPtsConfiguration);
                    }
                }
            }
            // Clear monitoring data.
            measurementTracking.reset();
            log.info("Delivery finalized for PTS: {}, Tank: {}", ptsId, tankId);
        }
    }
    private void recalculateTankLevelPerSalesOnDelayedDelivery(TankDelivery tankDelivery, ControllerPtsConfiguration controllerPtsConfiguration) {
        Long tankId = tankDelivery.getTank().getIdConf();
        LocalDateTime startDateTime = tankDelivery.getStartDateTime();
        LocalDateTime endDateTime = tankDelivery.getEndDateTime();
        TankDelivery nextDelivery = findNextDeliveryByTankIdConfAndPtsId(controllerPtsConfiguration.getPtsId(), tankId,endDateTime);
        List<TankLevelPerSales> recordsAfterTankFilling;
        if (nextDelivery !=null){
            recordsAfterTankFilling = tankLevelPerSalesService.findByTankAndDateRange(controllerPtsConfiguration.getPtsId(),tankId,startDateTime,nextDelivery.getStartDateTime());
        }else {
            recordsAfterTankFilling = tankLevelPerSalesService.findByTankAndDate(controllerPtsConfiguration.getPtsId(), tankId, startDateTime);
        }
        if (!recordsAfterTankFilling.isEmpty()) {
            log.info("Recalcul des points TankLevelPerSales pour le Tank: {}", tankId);
            for (TankLevelPerSales tankLevel : recordsAfterTankFilling) {
                // Calculer la nouvelle valeur de volume en fonction du volume livré
                double newVolume = tankLevel.getTankVolumeChanges() + tankDelivery.getProductVolume();
                tankLevel.setTankVolumeChanges(newVolume);
                tankLevel.setAudit(new AuditableEntity<>());
                tankLevelPerSalesService.save(tankLevel);
            }
        }
    }
}
