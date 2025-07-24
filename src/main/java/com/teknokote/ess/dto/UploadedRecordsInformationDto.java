package com.teknokote.ess.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadedRecordsInformationDto {

    private Long pumpTransactionsUploaded;
    private Long pumpTransactionsTotal;
    private Long tankMeasurementsUploaded;
    private Long tankMeasurementsTotal;
    private Long inTankDeliveriesUploaded;
    private Long inTankDeliveriesTotal;
    private Long gpsRecordsUploaded;
    private Long gpsRecordsTotal;
    private Long alertRecordsUploaded;
    private Long alertRecordsTotal;
    @Builder
    public UploadedRecordsInformationDto(Long pumpTransactionsUploaded, Long pumpTransactionsTotal, Long tankMeasurementsUploaded, Long tankMeasurementsTotal, Long inTankDeliveriesUploaded, Long inTankDeliveriesTotal, Long gpsRecordsUploaded, Long gpsRecordsTotal, Long alertRecordsUploaded, Long alertRecordsTotal) {
        this.pumpTransactionsUploaded = pumpTransactionsUploaded;
        this.pumpTransactionsTotal = pumpTransactionsTotal;
        this.tankMeasurementsUploaded = tankMeasurementsUploaded;
        this.tankMeasurementsTotal = tankMeasurementsTotal;
        this.inTankDeliveriesUploaded = inTankDeliveriesUploaded;
        this.inTankDeliveriesTotal = inTankDeliveriesTotal;
        this.gpsRecordsUploaded = gpsRecordsUploaded;
        this.gpsRecordsTotal = gpsRecordsTotal;
        this.alertRecordsUploaded = alertRecordsUploaded;
        this.alertRecordsTotal = alertRecordsTotal;
    }
}
