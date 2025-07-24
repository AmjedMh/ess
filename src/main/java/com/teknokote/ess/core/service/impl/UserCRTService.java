package com.teknokote.ess.core.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teknokote.ess.core.model.UserHistory;
import jakarta.servlet.http.HttpServletRequest;


public interface UserCRTService {

    UserHistory saveUserHistory(UserHistory userHistory, HttpServletRequest request);
    String getSimpleNameOfRequestPacket(String message) throws JsonProcessingException;

}
