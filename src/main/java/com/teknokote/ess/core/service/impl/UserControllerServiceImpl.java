package com.teknokote.ess.core.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.core.model.UserHistory;
import com.teknokote.pts.client.request.JsonPTSQuery;
import com.teknokote.pts.client.request.RequestPacket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@Slf4j
public class UserControllerServiceImpl implements UserCRTService {

    @Override
    public UserHistory saveUserHistory(UserHistory userHistory, HttpServletRequest request)
    {
        userHistory.setActivityDate(LocalDateTime.now());
        userHistory.setRequestURI(request.getRequestURI());
        userHistory.setIpAddress(request.getRemoteAddr());
        return null;
    }

    @Override
    public String getSimpleNameOfRequestPacket(String message) throws JsonProcessingException {
        JsonPTSQuery query = new ObjectMapper().readerFor(JsonPTSQuery.class).readValue(message);
        RequestPacket requestPacket = query.getPackets().get(0);

        String packetSimpleName = requestPacket.getClass().getSimpleName();
        if (packetSimpleName.equals("PumpAuthorizeRequestPacket")) {
            return packetSimpleName;
        } else if (packetSimpleName.equals("SetPumpNozzlesConfigurationRequestPacket")) {
            return packetSimpleName;
        } else if (packetSimpleName.equals("SetPumpsConfigurationRequestPacket")) {
            return packetSimpleName;
        } else if (packetSimpleName.equals("SetProbesConfigurationRequestPacket")) {
            return packetSimpleName;
        } else if (packetSimpleName.equals("SetFuelGradesConfigurationRequestPacket")) {
            return packetSimpleName;
        } else if (packetSimpleName.equals("SetTanksConfigurationRequestPacket")) {
            return packetSimpleName;
        } else if (packetSimpleName.equals("SetReadersConfigurationRequestPacket")) {
            return packetSimpleName;
        } else {
            log.info("No updates found");
            return "";
        }
    }

}


