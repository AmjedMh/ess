package com.teknokote.ess.core.service.impl.transactions;

import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.core.model.movements.TankLevelPerSales;
import com.teknokote.ess.core.model.movements.TankMeasurement;
import com.teknokote.ess.core.repository.TankLevelPerSalesRepository;
import com.teknokote.ess.core.repository.tank_delivery.TankDeliveryRepository;
import com.teknokote.ess.core.repository.tank_measurement.TankMeasurementRepository;
import com.teknokote.ess.dto.charts.TankLevelPerSalesChartDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TankLevelPerSalesService {
    @Autowired
    private TankLevelPerSalesRepository tankLevelPerSalesRepository;
    //dependance circulaire
    @Autowired
    private TankDeliveryRepository tankDeliveryRepository;
    @Autowired
    private TankMeasurementRepository tankMeasurementRepository;

    public Optional<TankLevelPerSales> getLastTankLevelPerSales(Long tankId, Long idCtr) {
        return tankLevelPerSalesRepository.getLastTankLevelPerSales(tankId,idCtr);
    }

    public TankLevelPerSales save(TankLevelPerSales tankLevelPerSales) {
        return tankLevelPerSalesRepository.save(tankLevelPerSales);
    }

    public List<TankLevelPerSalesChartDto> findAllByControllerPtsIdAndTankPeriod(Long idCtr, String tank, LocalDateTime startDate, LocalDateTime endDate) {
        return tankLevelPerSalesRepository.findAllByControllerPtsIdAndTankPeriod(idCtr,tank,startDate,endDate);
    }

    public TankLevelPerSales findTankLevelPerSalesByPumpTransactionId(Long idCtr, String tank, Long transactionId) {
        return tankLevelPerSalesRepository.findTankLevelPerSalesByPumpTransactionId(idCtr,tank,transactionId);
    }

    public List<TankLevelPerSales> getTankLeveChangesByTank(Long idCtr, String tank) {
        return tankLevelPerSalesRepository.getTankLeveChangesByTank(idCtr,tank);
    }

    public void saveTankLevelChanges(PumpTransaction pumpTransaction) {
        Double previousTankVolume = calculatePreviousTankVolume(pumpTransaction);
        if (previousTankVolume !=null) {
            createNewTankLevelPerSales(pumpTransaction, previousTankVolume);
        }
    }

    private Double calculatePreviousTankVolume(PumpTransaction pumpTransaction){
        Long tankId = pumpTransaction.getNozzle().getTank().getIdConf();
        String ptsId = pumpTransaction.getControllerPts().getPtsId();
        // Check if there is already a TankLevelPerSales entry for the tank
        Optional<TankLevelPerSales> latestTankLevelPerSales = this.getLastTankLevelPerSales(tankId, pumpTransaction.getControllerPts().getId());
        double previousTankVolume;
        if (latestTankLevelPerSales.isPresent()) {
            // Use the existing TankLevelPerSales entry to get the previous volume
            TankLevelPerSales lastTankLevelPerSales = latestTankLevelPerSales.get();
            previousTankVolume = lastTankLevelPerSales.getTankVolumeChanges(); // The last recorded tank volume
            return previousTankVolume;
        } else {
            Optional<TankMeasurement> lastMeasurement = tankMeasurementRepository.getLastMeasurementByDate(ptsId, tankId,pumpTransaction.getDateTime());
            if (lastMeasurement.isPresent()) {
                return lastMeasurement.get().getProductVolume();
            } else {
                // Handle the case when no previous measurement exists
                log.info("No previous tank measurement found for tankId: " + tankId);
            }
        }
        return null;
    }

    /**
     * Create and save a new TankLevelPerSales entry based on PumpTransaction and previous volume.
     */
    private void createNewTankLevelPerSales(PumpTransaction pumpTransaction, double previousTankVolume) {
        Long tankId = pumpTransaction.getNozzle().getTank().getIdConf();
        LocalDateTime transactionTime = pumpTransaction.getDateTime();

        // Get the current tank volume after the transaction based on the sales
        double salesVolume = pumpTransaction.getVolume();
        double currentTankVolume = previousTankVolume - salesVolume;

        // Create the new TankLevelPerSales entry
        TankLevelPerSales tankLevelPerSales = new TankLevelPerSales();
        tankLevelPerSales.setTank(tankId);
        tankLevelPerSales.setFuelGrade(pumpTransaction.getFuelGrade().getName());
        tankLevelPerSales.setPumpTransactionId(pumpTransaction.getId());
        tankLevelPerSales.setSalesVolume(salesVolume); // Sales volume from the pump
        tankLevelPerSales.setTankVolumeChanges(currentTankVolume);  // Updated tank volume after the transaction
        tankLevelPerSales.setDateTime(transactionTime);
        tankLevelPerSales.setControllerPts(pumpTransaction.getControllerPtsConfiguration().getControllerPts());
        tankLevelPerSales.setControllerPtsConfiguration(pumpTransaction.getControllerPtsConfiguration());
        this.save(tankLevelPerSales);
    }

    public List<TankLevelPerSales> findByTankAndDateRange(String ptsId, Long tankId,LocalDateTime startDateTime,LocalDateTime endDateTime) {
        return tankLevelPerSalesRepository.findByTankAndDateRange(ptsId,tankId,startDateTime,endDateTime);
    }

    public List<TankLevelPerSales> findByTankAndDate(String ptsId, Long tankId,LocalDateTime endDateTime) {
        return tankLevelPerSalesRepository.findByTankAndDate(ptsId,tankId,endDateTime);
    }
}
