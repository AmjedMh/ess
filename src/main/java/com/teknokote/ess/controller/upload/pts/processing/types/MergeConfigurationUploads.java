package com.teknokote.ess.controller.upload.pts.processing.types;

import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.repository.ControllePtsRepository;
import com.teknokote.ess.core.service.impl.*;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.ResponsePacket;
import com.teknokote.pts.client.upload.configuration.UploadConfigRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MergeConfigurationUploads {

    public static final String JSON_PTS_PROTOCOL = "jsonPTS";
    @Autowired
    private FuelGradesServiceImpl fuelGradesService;
    @Autowired
    private ProbeService probeService;
    @Autowired
    private TankService tankService;
    @Autowired
    private NozzleService nozzleService;
    @Autowired
    private PumpService pumpService;
    @Autowired
    private PtsNetworkSettingService ptsNetworkSettingService;
    @Autowired
    private RemoteServerService remoteServerService;
    @Autowired
    private ReaderService readerService;
    @Autowired
    private ControllerService controllerService;
    @Autowired
    private ControllePtsRepository controllePtsRepository;

    /**
     * Important: Arrivant à cette méthode, celà suppose que le contrôle de configuration niveau application est inexistant.
     */
    @Transactional
    public void uploadConfig(UploadConfigRequest uploadConfigRequest, String ptsId) {

        ControllerPts controllerPts = controllerService.findController(ptsId);
        final String configurationId = uploadConfigRequest.getPackets().get(0).getConfigurationId();
        ControllerPtsConfiguration newUploadedControllerConfiguration;
        controllerPts.addNewConfiguration(configurationId);
        controllerPts = controllePtsRepository.save(controllerPts);

        newUploadedControllerConfiguration = controllerPts.getControllerPtsConfigurations()
                .stream().filter(el -> el.getConfigurationId().equals(configurationId))
                .findFirst().orElseThrow(RuntimeException::new);

        JsonPTSResponse jsonPTS1;
        JsonPTSResponse jsonPTS2;
        JsonPTSResponse jsonPTS3;
        JsonPTSResponse jsonPTS4;
        JsonPTSResponse jsonPTS5;
        JsonPTSResponse jsonPTS6;
        JsonPTSResponse jsonPTS7;
        JsonPTSResponse jsonPTS8;
        jsonPTS1 = jsonPTS2 = jsonPTS3 = jsonPTS4 = jsonPTS5 = jsonPTS6 = jsonPTS7 = jsonPTS8 = null;

        for (ResponsePacket responsePacket : uploadConfigRequest.getPackets().get(0).getConfiguration()) {
            switch (responsePacket.getClass().getSimpleName()) {
                case "PTSFuelGradesConfigurationResponsePacket":
                    jsonPTS1 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                    break;
                case "PTSTankConfigurationResponsePacket":
                    jsonPTS2 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                    break;
                case "PTSProbesConfigurationResponsePacket":
                    jsonPTS3 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                    break;
                case "PTSPumpsConfigurationResponsePacket":
                    jsonPTS4 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                    break;
                case "PTSPumpNozzlesResponsePacket":
                    jsonPTS5 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                    break;
                case "PtsNetworkSettingResponsePacket":
                    jsonPTS6 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                    break;
                case "PTSRemoteServerResponsePacket":
                    jsonPTS7 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                    break;
                case "PTSReadersConfigurationResponsePacket":
                    jsonPTS8 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                    break;
                default:
                    break;
            }
        }
        /**
         * - PriceBoardsConfiguration
         * - TagsList               -->q
         * - DateTime               --> synchro date time
         * - DailyProcessingTime
         * - WirelessDevicesConfiguration
         *
         * Done:
         *                              PtsNetworkSettings",
         *                              ReadersConfiguration,
         *                     "Type": "RemoteServerConfiguration",
         *                     "Type": "PumpsConfiguration",
         *                     "Type": "ProbesConfiguration",
         *                      "Type": "FuelGradesConfiguration",
         *                     "Type": "TanksConfiguration",
         *                     "Type": "PumpNozzlesConfiguration",
         *
         *  Je ne vois pas pour:
         *  UploadInTankDelivery:
         *  - les deux valeurs : StartValues et EndValues ==> il faut garder les deux à mon avis
         *
         */
        ptsNetworkSettingService.addNewNetwork(newUploadedControllerConfiguration, jsonPTS6);
        remoteServerService.addNewRemote(newUploadedControllerConfiguration, jsonPTS7);
        readerService.addNewReader(newUploadedControllerConfiguration, jsonPTS8);
        fuelGradesService.addNewFuelGrades(newUploadedControllerConfiguration, jsonPTS1);
        pumpService.addNewPumpPortC(newUploadedControllerConfiguration, jsonPTS4);
        tankService.addNewTank(newUploadedControllerConfiguration, jsonPTS2);
        probeService.addProbes(newUploadedControllerConfiguration, jsonPTS3);
        nozzleService.addNewPumpNozzles(newUploadedControllerConfiguration, jsonPTS5);
        controllePtsRepository.save(controllerPts);
    }


    @Transactional
    public void uploadConfigTraineeship(UploadConfigRequest uploadConfigRequest) {

        List<ControllerPts> controllerPts = controllePtsRepository.findAll();
        if (!controllerPts.isEmpty()) {
            for (ControllerPts controllerPts1 : controllerPts) {

                final String configurationId = uploadConfigRequest.getPackets().get(0).getConfigurationId();
                ControllerPtsConfiguration newUploadedControllerConfiguration;
                controllerPts1.addNewConfiguration(configurationId);
                controllerPts1 = controllePtsRepository.save(controllerPts1);

                newUploadedControllerConfiguration = controllerPts1.getControllerPtsConfigurations()
                        .stream().filter(el -> el.getConfigurationId().equals(configurationId))
                        .findFirst().orElseThrow(RuntimeException::new);

                JsonPTSResponse jsonPTS1;
                JsonPTSResponse jsonPTS2;
                JsonPTSResponse jsonPTS3;
                JsonPTSResponse jsonPTS4;
                JsonPTSResponse jsonPTS5;
                JsonPTSResponse jsonPTS6;
                JsonPTSResponse jsonPTS7;
                JsonPTSResponse jsonPTS8;
                jsonPTS1 = jsonPTS2 = jsonPTS3 = jsonPTS4 = jsonPTS5 = jsonPTS6 = jsonPTS7 = jsonPTS8 = null;

                for (ResponsePacket responsePacket : uploadConfigRequest.getPackets().get(0).getConfiguration()) {
                    switch (responsePacket.getClass().getSimpleName()) {
                        case "PTSFuelGradesConfigurationResponsePacket":
                            jsonPTS1 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                            break;
                        case "PTSTankConfigurationResponsePacket":
                            jsonPTS2 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                            break;
                        case "PTSProbesConfigurationResponsePacket":
                            jsonPTS3 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                            break;
                        case "PTSPumpsConfigurationResponsePacket":
                            jsonPTS4 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                            break;
                        case "PTSPumpNozzlesResponsePacket":
                            jsonPTS5 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                            break;
                        case "PtsNetworkSettingResponsePacket":
                            jsonPTS6 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                            break;
                        case "PTSRemoteServerResponsePacket":
                            jsonPTS7 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                            break;
                        case "PTSReadersConfigurationResponsePacket":
                            jsonPTS8 = JsonPTSResponse.builder().protocol(JSON_PTS_PROTOCOL).packets(List.of(responsePacket)).build();
                            break;
                        default:
                            break;
                    }
                }
                ptsNetworkSettingService.addNewNetwork(newUploadedControllerConfiguration, jsonPTS6);
                remoteServerService.addNewRemote(newUploadedControllerConfiguration, jsonPTS7);
                readerService.addNewReader(newUploadedControllerConfiguration, jsonPTS8);
                fuelGradesService.addNewFuelGrades(newUploadedControllerConfiguration, jsonPTS1);
                pumpService.addNewPumpPortC(newUploadedControllerConfiguration, jsonPTS4);
                tankService.addNewTank(newUploadedControllerConfiguration, jsonPTS2);
                probeService.addProbes(newUploadedControllerConfiguration, jsonPTS3);
                nozzleService.addNewPumpNozzles(newUploadedControllerConfiguration, jsonPTS5);

                controllePtsRepository.save(controllerPts1);
            }
        }
    }
}
