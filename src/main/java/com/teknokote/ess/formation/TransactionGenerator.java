package com.teknokote.ess.formation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.core.dao.mappers.ControllerPtsMapper;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Nozzle;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.core.model.organization.EnumAffectationMode;
import com.teknokote.ess.core.repository.ControllePtsRepository;
import com.teknokote.ess.core.repository.configuration.ControllerPtsConfigurationRepository;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.dto.ControllerPtsDto;
import com.teknokote.pts.client.upload.pump.JsonPumpUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "ess", name = "mode", havingValue = "formation", matchIfMissing = true)
public class TransactionGenerator {
    @Autowired
    private ControllePtsRepository controllePtsRepository;
    @Autowired
    private ControllerPtsConfigurationRepository controllerPtsConfigurationRepository;
    @Autowired
    private ControllerPtsMapper mapper;
    @Autowired
    private TransactionService transactionService;

    @Value("${ess.transaction.quantity.min}")
    private int minQuantity;

    @Value("${ess.transaction.quantity.max}")
    private int maxQuantity;

    @Value("${ess.transaction.tag}")
    private String tag;
    private final Random random = new Random();

    @Transactional
    @Scheduled(fixedDelay = 180000) // 3 minutes in milliseconds
    public void generateRandomPumpUpload() throws JsonProcessingException {
        List<ControllerPtsDto> controllerPtsList = controllePtsRepository.findAll().stream().map(mapper::toDto).toList();

        for (ControllerPtsDto controllerPts : controllerPtsList) {
            Optional<ControllerPtsConfiguration> configOpt = controllerPtsConfigurationRepository.findCurrentConfigurationOnController(controllerPts.getId());

            if (configOpt.isPresent()) {
                ControllerPtsConfiguration controllerPtsConfiguration = configOpt.get();

                if (controllerPtsConfiguration.getPumps() != null && !controllerPtsConfiguration.getPumps().isEmpty()) {
                    int pumpSize = controllerPtsConfiguration.getPumps().size();

                    // Choose a random pump
                    int randomPumpIndex = random.nextInt(pumpSize);
                    Pump randomPump = controllerPtsConfiguration.getPumps().get(randomPumpIndex);

                    List<Nozzle> nozzleList = randomPump.getNozzleList();
                    if (nozzleList != null && !nozzleList.isEmpty()) {
                        int nozzleSize = nozzleList.size();

                        // Choose a random nozzle
                        int randomNozzleIndex = random.nextInt(nozzleSize);
                        Nozzle randomNozzle = nozzleList.get(randomNozzleIndex);

                        generatePumpUpload(controllerPtsConfiguration, LocalDateTime.now(), randomPump, randomNozzle);
                    }
                }
            }
        }
    }

    private void generatePumpUpload(ControllerPtsConfiguration controllerPtsConfiguration, LocalDateTime dateTime, Pump currentPump, Nozzle currentNozzle) throws JsonProcessingException {
        String fuelGradeName = currentNozzle.getGrade().getName();
        Double price = currentNozzle.getGrade().getPrice();

        int quantity = random.nextInt(maxQuantity - minQuantity + 1) + minQuantity;
        int transaction = random.nextInt(100000) + 1;
        int id = random.nextInt(100000) + 1;

        // Retrieve the last transaction for the given pump and nozzle index
        Optional<PumpTransaction> lastTransactionOptional = transactionService.findLastTransactionOnDate(controllerPtsConfiguration.getPtsId(), currentNozzle.getIdConf(), currentPump.getIdConf(), dateTime);
        BigDecimal previousTotalVolume = BigDecimal.ZERO;
        BigDecimal previousTotalAmount = BigDecimal.ZERO;

        if (lastTransactionOptional.isPresent()) {
            PumpTransaction lastTransaction = lastTransactionOptional.get();
            previousTotalVolume = lastTransaction.getTotalVolume();
            previousTotalAmount = lastTransaction.getTotalAmount();
        }
        double currentVolume = quantity;
        double currentAmount = quantity * price;
        double newTotalVolume = previousTotalVolume.doubleValue() + currentVolume;
        double newTotalAmount = previousTotalAmount.doubleValue() + currentAmount;
        uploadPumpTransaction(controllerPtsConfiguration, controllerPtsConfiguration.getConfigurationId(), dateTime, fuelGradeName, price, quantity, transaction, id, Math.toIntExact(currentPump.getIdConf()), Math.toIntExact(currentNozzle.getIdConf()), newTotalVolume, currentAmount, newTotalAmount);
    }

    public void uploadPumpTransaction(ControllerPtsConfiguration controllerPtsConfiguration, String configurationId, LocalDateTime randomDateTime, String fuelGradeName, Double price, int quantity, int transaction, int id, int pumpIndex, int nozzleIndex, double totalVolume, double currentAmount, double totalAmount) throws JsonProcessingException {
        String uploadedBody;

        String[] tags = tag.split(",");

        // Alternate between the tags
        String currentTag = tags[transaction % 8];


        if (controllerPtsConfiguration.getControllerPts().getStation().getModeAffectation().equals(EnumAffectationMode.AUTOMATIQUE)) {
            uploadedBody = "{\"Protocol\":\"jsonPTS\",\"PtsId\":\"" + controllerPtsConfiguration.getControllerPts().getPtsId() + "\",\"Packets\":[{\"Id\":" + id + ",\"Type\":\"UploadPumpTransaction\",\"Data\":{\"DateTime\":\"" + randomDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\"Pump\":" + pumpIndex + ",\"Nozzle\":" + nozzleIndex + ",\"FuelGradeId\":" + nozzleIndex + ",\"FuelGradeName\":\"" + fuelGradeName + "\",\"Transaction\":" + transaction + ",\"UserId\":1,\"Volume\":" + quantity + ",\"Amount\":" + currentAmount + ",\"Price\":" + price + ",\"TotalVolume\":" + totalVolume + ",\"TotalAmount\":" + totalAmount + ",\"DateTimeStart\":\"" + randomDateTime.minusSeconds(12).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\"TCVolume\":0,\"Tag\":\"" + currentTag + "\",\"ConfigurationId\":\"" + configurationId + "\"}}]}";
        } else {
            uploadedBody = "{\"Protocol\":\"jsonPTS\",\"PtsId\":\"" + controllerPtsConfiguration.getControllerPts().getPtsId() + "\",\"Packets\":[{\"Id\":" + id + ",\"Type\":\"UploadPumpTransaction\",\"Data\":{\"DateTime\":\"" + randomDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\"Pump\":" + pumpIndex + ",\"Nozzle\":" + nozzleIndex + ",\"FuelGradeId\":" + nozzleIndex + ",\"FuelGradeName\":\"" + fuelGradeName + "\",\"Transaction\":" + transaction + ",\"UserId\":1,\"Volume\":" + quantity + ",\"Amount\":" + currentAmount + ",\"Price\":" + price + ",\"TotalVolume\":" + totalVolume + ",\"TotalAmount\":" + totalAmount + ",\"DateTimeStart\":\"" + randomDateTime.minusSeconds(12).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\"TCVolume\":0,\"Tag\":\"\",\"ConfigurationId\":\"" + configurationId + "\"}}]}";
        }

        JsonPumpUpload pumpUpload = new ObjectMapper().readValue(uploadedBody, JsonPumpUpload.class);
        transactionService.processUploadedTransaction(pumpUpload, controllerPtsConfiguration);
    }

}


