package com.teknokote.ess.events.listeners;

import com.teknokote.ess.core.dao.shifts.ShiftRotationDao;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.service.cache.PumpStatusCache;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.dto.PeriodDto;
import com.teknokote.ess.dto.data.EnumPumpStatus;
import com.teknokote.ess.dto.data.FuelDataIndexStart;
import com.teknokote.ess.dto.data.FuelStatusData;
import com.teknokote.ess.dto.data.PumpStatusDto;
import com.teknokote.pts.client.upload.status.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PumpStatusService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private PumpStatusCache pumpStatusCache;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ShiftRotationDao shiftRotationDao;

    public void pumpStationStatus(UploadStatus uploadStatus, ControllerPtsConfiguration controllerPtsConfiguration) {
        List<PumpStatusDto> pumpStatusList = createPumpStatusDtos(uploadStatus, controllerPtsConfiguration);
        String ptsId = controllerPtsConfiguration.getPtsId();
        pumpStatusCache.updateStationPumpStatusMap(ptsId, pumpStatusList);
        messagingTemplate.convertAndSend("/topic/pumpStatus", pumpStatusCache.getStationPumpStatusMap());
    }

    public List<PumpStatusDto> createPumpStatusDtos(UploadStatus uploadStatus, ControllerPtsConfiguration controllerPtsConfiguration) {
        List<PumpStatusDto> pumpStatusDtos = new ArrayList<>();

        processPumpData(uploadStatus.getPumps(), controllerPtsConfiguration.getPtsId(), controllerPtsConfiguration.getConfigurationId(), pumpStatusDtos, uploadStatus.getDateTime());
        pumpStatusDtos.sort(Comparator.comparing(PumpStatusDto::getPumpId));

        return pumpStatusDtos;
    }

    public void processPumpData(Pumps pumps, String ptsId, String configurationId, List<PumpStatusDto> pumpStatusDtos, String dateTime) {
        LocalDateTime currentDateTime = LocalDateTime.parse(dateTime);
        if (pumps.getFillingStatus() != null && pumps.getFillingStatus().getIds() != null) {
            addPumpStatusDtosFromFillingStatus(pumps.getFillingStatus(), ptsId, configurationId, pumpStatusDtos, currentDateTime);
        }
        if (pumps.getIdleStatus() != null && pumps.getIdleStatus().getIds() != null) {
            addPumpStatusDtosFromIdleStatus(pumps.getIdleStatus(), ptsId, configurationId, pumpStatusDtos, currentDateTime);
        }
        if (pumps.getOfflineStatus() != null && pumps.getOfflineStatus().getIds() != null) {
            addPumpStatusDtosFromOfflineStatus(pumps.getOfflineStatus(), ptsId, configurationId, pumpStatusDtos);
        }
        if (pumps.getOfflineStatus() != null && pumps.getEndOfTransactionStatus().getIds() != null) {
            addPumpStatusDtosFromEndOfTransactionStatus(pumps.getEndOfTransactionStatus(), ptsId, configurationId, pumpStatusDtos, currentDateTime);
        }
    }

    public void addPumpStatusDtosFromIdleStatus(IdleStatus idleStatus, String ptsId, String configurationId, List<PumpStatusDto> pumpStatusDtos, LocalDateTime dateTime) {
        for (int i = 0; i < idleStatus.getIds().size(); i++) {
            Long pumpId = idleStatus.getIds().get(i);
            Double lastVolume = idleStatus.getLastVolumes().get(i);
            Double lastAmount = idleStatus.getLastAmounts().get(i);
            Long lastNozzle = idleStatus.getLastNozzles().get(i);
            BigDecimal receivedTotalVolume = idleStatus.getLastTotalVolumes() != null ? idleStatus.getLastTotalVolumes().get(i) : null;
            BigDecimal receivedTotalAmount = idleStatus.getLastTotalAmounts() != null ? idleStatus.getLastTotalAmounts().get(i) : null;
            Double lastPrice = idleStatus.getLastPrices().get(i);

            PumpStatusDto pumpStatusDto = createPumpStatusDto(ptsId, pumpId, EnumPumpStatus.IDLE, configurationId);

            // Create a list to hold FuelStatusData for each fuel grade
            List<FuelStatusData> fuelStatusDataList = new ArrayList<>();

            // Fetch primary fuel grade
            FuelGrade primaryFuelGrade = pumpStatusCache.updateFuelGradeCache(ptsId, pumpId, lastNozzle, configurationId);
            // Check if FuelStatusData for this fuel grade is cached
            List<FuelStatusData> cachedFuelDataList = pumpStatusCache.getLastIdleStatusCache(ptsId, pumpId);
            FuelStatusData cachedPrimaryFuelData = cachedFuelDataList != null
                    ? cachedFuelDataList.stream()
                    .filter(data -> data.getFuelGradeId().equals(primaryFuelGrade.getIdConf()))
                    .findFirst()
                    .orElse(null)
                    : null;

            BigDecimal initialVolumeCashed = (cachedPrimaryFuelData != null) ? cachedPrimaryFuelData.getInitialTotalVolume() : null;
            BigDecimal initialAmountCashed = (cachedPrimaryFuelData != null) ? cachedPrimaryFuelData.getInitialTotalAmount() : null;

            // Add primary fuel data to list
            FuelStatusData primaryFuelData = createFuelStatusData(primaryFuelGrade.getIdConf(), primaryFuelGrade.getName(), lastVolume, lastAmount, receivedTotalVolume, receivedTotalAmount, lastPrice, initialVolumeCashed, initialAmountCashed, pumpId, ptsId, dateTime);
            fuelStatusDataList.add(primaryFuelData);

            // Fetch additional fuel grades
            List<FuelGrade> additionalFuelGrades = pumpStatusCache.getAdditionalFuelGradesCache(ptsId, pumpId, configurationId).stream().filter(fuelGrade -> !fuelGrade.getIdConf().equals(primaryFuelGrade.getIdConf())).toList();

            for (FuelGrade fuelGrade : additionalFuelGrades) {
                FuelStatusData cachedFuelData = cachedFuelDataList != null
                        ? cachedFuelDataList.stream()
                        .filter(data -> data.getFuelGradeId().equals(fuelGrade.getIdConf()))
                        .findFirst()
                        .orElse(null)
                        : null;

                // Use cached values if available, otherwise default to 0
                Double cachedVolume = (cachedFuelData != null) ? cachedFuelData.getVolume() : null;
                Double cachedAmount = (cachedFuelData != null) ? cachedFuelData.getAmount() : null;
                BigDecimal cashedInitialVolume = (cachedFuelData != null) ? cachedFuelData.getInitialTotalVolume() : null;
                BigDecimal cashedInitialAmount = (cachedFuelData != null) ? cachedFuelData.getInitialTotalAmount() : null;
                BigDecimal cachedTotalVolume = (cachedFuelData != null) ? cachedFuelData.getTotalVolume() : null;
                BigDecimal cachedTotalAmount = (cachedFuelData != null) ? cachedFuelData.getTotalAmount() : null;
                Double cachedPrice = (cachedFuelData != null) ? cachedFuelData.getPrice() : null;
                FuelStatusData additionalFuelData = createFuelStatusData(fuelGrade.getIdConf(), fuelGrade.getName(), cachedVolume, cachedAmount, cachedTotalVolume, cachedTotalAmount, cachedPrice, cashedInitialVolume, cashedInitialAmount, pumpId, ptsId, dateTime);
                fuelStatusDataList.add(additionalFuelData);
            }

            // Cache all FuelStatusData objects for each pump
            pumpStatusCache.updateLastIdleStatusCache(ptsId, pumpId, fuelStatusDataList);
            pumpStatusDto.setFuelStatusData(fuelStatusDataList);
            pumpStatusDtos.add(pumpStatusDto);
        }
    }

    private void processTransactionStatus(
            List<Long> pumpIds, List<Double> volumes, List<Double> amounts, List<Double> prices,
            List<Long> fuelGradeIds, List<String> fuelGradeNames,
            EnumPumpStatus status, String ptsId, String configurationId,
            List<PumpStatusDto> pumpStatusDtos, LocalDateTime dateTime) {
        for (int i = 0; i < pumpIds.size(); i++) {
            Long pumpId = pumpIds.get(i);
            Double volume = volumes.get(i);
            Double amount = amounts.get(i);
            Double price = prices.get(i);
            Long fuelGradeId = fuelGradeIds.get(i);
            String fuelGradeName = fuelGradeNames.get(i);

            PumpStatusDto pumpStatusDto = createPumpStatusDto(ptsId, pumpId, status, configurationId);

            // Retrieve last idle status totals if available
            List<FuelStatusData> cachedFuelDataList = pumpStatusCache.getLastIdleStatusCache(ptsId, pumpId);
            FuelStatusData cachedIdleStatus = cachedFuelDataList != null
                    ? cachedFuelDataList.stream()
                    .filter(data -> data.getFuelGradeId().equals(fuelGradeId))
                    .findFirst()
                    .orElse(null) : null;
            BigDecimal cashedInitialVolume = (cachedIdleStatus != null) ? cachedIdleStatus.getInitialTotalVolume() : null;
            BigDecimal cashedInitialAmount = (cachedIdleStatus != null) ? cachedIdleStatus.getInitialTotalAmount() : null;
            BigDecimal totalVolume = (cachedIdleStatus != null) ? cachedIdleStatus.getTotalVolume() : null;
            BigDecimal totalAmount = (cachedIdleStatus != null) ? cachedIdleStatus.getTotalAmount() : null;

            FuelStatusData fuelData = createFuelStatusData(fuelGradeId, fuelGradeName, volume, amount, totalVolume, totalAmount, price, cashedInitialVolume, cashedInitialAmount, pumpId, ptsId, dateTime);

            List<FuelStatusData> fuelStatusDataList = new ArrayList<>();
            fuelStatusDataList.add(fuelData);
            pumpStatusDto.setFuelStatusData(fuelStatusDataList);
            pumpStatusDtos.add(pumpStatusDto);
        }
    }

    public void addPumpStatusDtosFromEndOfTransactionStatus(
            EndOfTransactionStatus endOfTransactionStatus, String ptsId, String configurationId, List<PumpStatusDto> pumpStatusDtos, LocalDateTime dateTime) {

        processTransactionStatus(
                endOfTransactionStatus.getIds(),
                endOfTransactionStatus.getVolumes(),
                endOfTransactionStatus.getAmounts(),
                endOfTransactionStatus.getPrices(),
                endOfTransactionStatus.getFuelGradeIds(),
                endOfTransactionStatus.getFuelGradeNames(),
                EnumPumpStatus.END_OF_TRANSACTION,
                ptsId,
                configurationId,
                pumpStatusDtos,
                dateTime
        );
    }

    public void addPumpStatusDtosFromFillingStatus(
            FillingStatus fillingStatus, String ptsId, String configurationId, List<PumpStatusDto> pumpStatusDtos, LocalDateTime dateTime) {

        processTransactionStatus(
                fillingStatus.getIds(),
                fillingStatus.getVolumes(),
                fillingStatus.getAmounts(),
                fillingStatus.getPrices(),
                fillingStatus.getFuelGradeIds(),
                fillingStatus.getFuelGradeNames(),
                EnumPumpStatus.FILLING,
                ptsId,
                configurationId,
                pumpStatusDtos,
                dateTime
        );
    }

    private void addPumpStatusDtosFromOfflineStatus(OfflineStatus offlineStatus, String ptsId, String configurationId, List<PumpStatusDto> pumpStatusDtos) {
        for (Long pumpId : offlineStatus.getIds()) {
            PumpStatusDto pumpStatusDto = createPumpStatusDto(ptsId, pumpId, EnumPumpStatus.OFFLINE, configurationId);
            pumpStatusDtos.add(pumpStatusDto);
        }
    }

    public PumpStatusDto createPumpStatusDto(String ptsId, Long pumpId, EnumPumpStatus pumpStatus, String configurationId) {
        PumpStatusDto pumpStatusDto = new PumpStatusDto();
        pumpStatusDto.setControllerPtsId(ptsId);
        pumpStatusDto.setPumpId(pumpId);
        pumpStatusDto.setPumpStatus(pumpStatus);

        // Use additionalFuelGradesCache to get the fuel grade IDs
        List<Long> fuelGradeIds = pumpStatusCache.additionalFuelGradesCache(ptsId, pumpId, configurationId);

        pumpStatusDto.setAttachedFuelGrades(fuelGradeIds);

        return pumpStatusDto;
    }


    public FuelStatusData createFuelStatusData(
            Long fuelGradeId, String fuelGradeName, Double volume, Double amount, BigDecimal totalVolume, BigDecimal totalAmount,
            Double price, BigDecimal initialTotalVolume, BigDecimal initialTotalAmount, Long pumpId, String ptsId, LocalDateTime dateTime) {

        FuelStatusData fuelData = new FuelStatusData();
        fuelData.setFuelGradeId(fuelGradeId);
        fuelData.setFuelGradeName(fuelGradeName);
        fuelData.setVolume(volume);
        fuelData.setAmount(amount);
        fuelData.setPrice(price);
        fuelData.setTotalVolume(totalVolume);
        fuelData.setTotalAmount(totalAmount);
        fuelData.setInitialTotalAmount(initialTotalAmount);
        fuelData.setInitialTotalVolume(initialTotalVolume);

        PeriodDto todayPeriod = getUpdatedPeriod(ptsId, pumpId, dateTime, fuelData);

        if (!dateTime.isBefore(todayPeriod.getStartDateTime())) {
            setInitialFuelDataIfNeeded(fuelData, pumpId, ptsId, fuelGradeId, todayPeriod);
            calculateTodaySales(fuelData);
        }

        return fuelData;
    }

    public PeriodDto getUpdatedPeriod(String ptsId, Long pumpId, LocalDateTime dateTime, FuelStatusData fuelStatusData) {
        PeriodDto todayPeriod = pumpStatusCache.getCachedPeriodDto(ptsId, pumpId, dateTime);

        if (dateTime.isAfter(todayPeriod.getStartDateTime().plusDays(1))) {
            todayPeriod = shiftRotationDao.dailyPeriodForStation(ptsId, dateTime);
            pumpStatusCache.updateCashedPeriodDto(ptsId, pumpId, todayPeriod);
            fuelStatusData.setTodaySalesAmount(BigDecimal.ZERO);
            fuelStatusData.setTodaySalesVolume(BigDecimal.ZERO);
            fuelStatusData.setInitialTotalVolume(null);
            fuelStatusData.setInitialTotalAmount(null);
        }
        return todayPeriod;
    }

    private void setInitialFuelDataIfNeeded(FuelStatusData fuelData, Long pumpId, String ptsId, Long fuelGradeId, PeriodDto todayPeriod) {
        if (fuelData.getInitialTotalVolume() == null || fuelData.getInitialTotalAmount() == null) {
            FuelDataIndexStart fuelDataIndexStart = transactionService.findInitialIndex(pumpId, ptsId, fuelGradeId, todayPeriod.getStartDateTime());

            if (fuelDataIndexStart != null) {
                fuelData.setInitialTotalVolume(
                        fuelDataIndexStart.getInitialTotalVolume() != null ? fuelDataIndexStart.getInitialTotalVolume() : BigDecimal.ZERO);
                fuelData.setInitialTotalAmount(
                        fuelDataIndexStart.getInitialTotalAmount() != null ? fuelDataIndexStart.getInitialTotalAmount() : BigDecimal.ZERO);
            } else {
                fuelData.setInitialTotalVolume(BigDecimal.ZERO);
                fuelData.setInitialTotalAmount(BigDecimal.ZERO);
            }
        }
    }

    private void calculateTodaySales(FuelStatusData fuelData) {
        if (fuelData.getTotalVolume() != null) {
            fuelData.setTodaySalesVolume(fuelData.getTotalVolume().subtract(fuelData.getInitialTotalVolume()));
        }
        if (fuelData.getTotalAmount() != null) {
            fuelData.setTodaySalesAmount(fuelData.getTotalAmount().subtract(fuelData.getInitialTotalAmount()));
        }
    }
}

