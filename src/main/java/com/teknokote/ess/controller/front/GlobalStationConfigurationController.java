package com.teknokote.ess.controller.front;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.impl.*;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping(EndPoints.CONFIGURATION_ROOT)
public class GlobalStationConfigurationController {
    @Autowired
    private TankService tankService;
    @Autowired
    private TankDeliveryService tankDeliveryService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private FuelGradesService fuelGradesService;
    @Autowired
    private ProbeService probeService;
    @Autowired
    private PumpService pumpService;
    @Autowired
    private NozzleService nozzleService;
    @Autowired
    private ReaderService readerService;
    @Autowired
    private FirmwareInformationService firmwareInformationService;
    @Autowired
    private TankMeasurementServices tankMeasurementServices;

    /*
     *
     * GetConfiguration from DataBase
     *
     */
    @GetMapping(EndPoints.CONFIGURATION_TANKS)
    public List<TankConfigDto> readTank(@PathVariable Long idCtr) {

        return tankService.findTankByControllerOnCurrentConfiguration(idCtr).stream()
                .map(tank -> tankService.mapToTankConfigDto(tank)).toList();
    }

    /**
     * Renvoie la liste des pompes du contrôleur tirée de la dernière configuration
     */
    @GetMapping(EndPoints.CONFIGURATION_PUMPS)
    public List<PumpsConfigDto> readPump(@PathVariable Long idCtr) {
        return pumpService.findPumpsByControllerOnCurrentConfiguration(idCtr).stream()
                .map(pump -> pumpService.mapToPumpConfigDto(pump)).toList();
    }

    @GetMapping(EndPoints.CONFIGURATION_PROBES)
    public List<ProbeConfigDto> readProbe(@PathVariable Long idCtr) {
        return probeService.findProbesByControllerOnCurrentConfiguration(idCtr).stream()
                .map(probe -> probeService.mapToProbeConfigDto(probe)).toList();
    }

    @GetMapping(EndPoints.CONFIGURATION_FUEL_GRADES)
    public List<FuelGradeConfigDto> readFuelGrade(@PathVariable Long idCtr) {
        return fuelGradesService.findFuelGradesByControllerOnCurrentConfiguration(idCtr).stream()
                .map(fuelGrade -> fuelGradesService.mapToFuelGradeConfigDto(fuelGrade)).toList();
    }

    @GetMapping(EndPoints.CONFIGURATION_NOZZLES)
    public List<NozzelConfigDto> readNozzle(@PathVariable Long idCtr) {
        return nozzleService.findNozzlesByControllerOnCurrentConfiguration(idCtr).stream()
                .map(nozzle -> nozzleService.mapToNozzelconfigDto(nozzle)).toList();
    }

    @GetMapping(EndPoints.CONFIGURATION_READERS)
    public List<ReaderDto> readReader(@PathVariable Long idCtr) {
        return readerService.findReadersByControllerOnCurrentConfiguration(idCtr).stream()
                .map(reader -> readerService.mapToReaderConfigDto(reader)).toList();
    }

    @PostMapping(EndPoints.LIST_OF_TRANSACTIONS)
    public Page<TransactionDto> getTransactions(@PathVariable Long idCtr,
                                                @RequestBody TransactionFilterDto filterDto,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size) {

        return transactionService.findTransactionsByFilter(
                idCtr, filterDto, page, size
        ).map(transactionService::mapToPumpTransactionDto);
    }

    @PostMapping(EndPoints.LIST_OF_TRANSACTIONS_EXCEL)
    public ResponseEntity<byte[]> generateEXCELOfTransactions(@PathVariable Long idCtr,
                                                  @RequestBody TransactionFilterDto filterDto,
                                                  @RequestParam String filterSummary,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "50") int size,
                                                  @RequestParam List<String> columnsToDisplay,@RequestParam String locale) throws IOException
    {
        final byte[] pdfContent = transactionService.generateExcelTransactionsByFilter(
           idCtr, filterDto, page, size,columnsToDisplay,locale,filterSummary
        );

        return ResponseEntity.ok()
           .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.xlsx")
           .contentType(MediaType.APPLICATION_OCTET_STREAM)
           .body(pdfContent);
    }
    @PostMapping(EndPoints.LIST_OF_TRANSACTIONS_PDF)
    public ResponseEntity<byte[]> generatePDFOfTransactions(@PathVariable Long idCtr,
                                                            @RequestBody TransactionFilterDto filterDto,
                                                            @RequestParam String filterSummary,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "50") int size,
                                                            @RequestParam List<String> columnsToDisplay,@RequestParam String locale) throws IOException

    {
        byte[] pdfContent = transactionService.generatePDFTransactionsByFilter(idCtr, filterDto, page, size, columnsToDisplay,locale,filterSummary);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @PostMapping(EndPoints.LIST_OF_DELIVERY)
    public Page<TankDeliveryDto> getTankDelivery(@PathVariable Long idCtr,
                                                @RequestBody TankFilterDto filterDto,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size) {

        return tankDeliveryService.findDeliveryByFilter(
                idCtr, filterDto, page, size
        ).map(tankDeliveryService::mapDeliveryToDto);
    }
    @PostMapping(EndPoints.LIST_OF_DELIVERY_EXCEL)
    public ResponseEntity<byte[]> generateEXCELOfDelivery(@PathVariable Long idCtr,
                                                          @RequestBody TankFilterDto filterDto,
                                                          @RequestParam String filterSummary,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "50") int size,
                                                          @RequestParam List<String> columnsToDisplay,@RequestParam String locale) throws IOException
    {
        final byte[] excelContent = tankDeliveryService.generateExcelDeliveryByFilter(
                idCtr, filterDto, page, size,columnsToDisplay,locale,filterSummary
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tankDelivery.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelContent);
    }

    @PostMapping(EndPoints.LIST_OF_DELIVERY_PDF)
    public ResponseEntity<byte[]> generatePDFOfTankDelivery(@PathVariable Long idCtr,
                                                            @RequestBody TankFilterDto filterDto,
                                                            @RequestParam String filterSummary,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "50") int size,
                                                            @RequestParam List<String> columnsToDisplay,@RequestParam String locale) throws IOException

    {
        byte[] pdfContent = tankDeliveryService.generatePDFTankDeliveryByFilter(idCtr, filterDto, page, size, columnsToDisplay,locale,filterSummary);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @PostMapping(EndPoints.LIST_OF_MEASUREMENT)
    public Page<TankMeasurementsDto> getTankMeasurements(@PathVariable Long idCtr,
                                                         @RequestBody TankFilterDto filterDto,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "50") int size) {

        return  tankMeasurementServices.findMeasurementByFilter(idCtr, filterDto, page, size)
                .map(tankMeasurementServices::mapTankMeasurementToDto);
    }

    @PostMapping(EndPoints.LIST_OF_MEASUREMENT_EXCEL)
    public ResponseEntity<byte[]> generateEXCELOfMeasurement(@PathVariable Long idCtr,
                                                          @RequestBody TankFilterDto filterDto,
                                                          @RequestParam String filterSummary,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "50") int size,
                                                          @RequestParam List<String> columnsToDisplay,@RequestParam String locale) throws IOException
    {
        final byte[] excelContent = tankMeasurementServices.generateExcelMeasurementByFilter(
                idCtr, filterDto, page, size,columnsToDisplay,locale,filterSummary
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tankMeasurement.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelContent);
    }

    @PostMapping(EndPoints.LIST_OF_MEASUREMENT_PDF)
    public ResponseEntity<byte[]> generatePDFOfTankMeasurement(@PathVariable Long idCtr,
                                                            @RequestBody TankFilterDto filterDto,
                                                            @RequestParam String filterSummary,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "50") int size,
                                                            @RequestParam List<String> columnsToDisplay,@RequestParam String locale) throws IOException

    {
        byte[] pdfContent = tankMeasurementServices.generatePDFTankMeasurementByFilter(idCtr, filterDto, page, size, columnsToDisplay,locale,filterSummary);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tankMeasurement.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @GetMapping(EndPoints.LIST_OF_NOZZLES_BY_PUMP)
    public List<NozzelConfigDto> readNozzleByPump(@PathVariable Long idCtr, @PathVariable long idPump) {
        return nozzleService.findNozzleByPump(idCtr, idPump).stream().
                map(nozzle -> nozzleService.mapToNozzelconfigDto(nozzle)).toList();
    }


    @GetMapping(EndPoints.VERSION)
    public FirmwareInformationDto getFirmwareVersion(@RequestParam String ptsId) {

        return firmwareInformationService.currentFirmwareVersion(ptsId);
    }

}
