package com.teknokote.ess.core.service.Entities;

import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.model.requests.EnumRequestStatus;
import com.teknokote.ess.core.model.requests.EnumRequestType;
import com.teknokote.ess.core.model.requests.FuelGradePriceChangeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AbstractRequestTest {
    private FuelGradePriceChangeRequest request;

    @BeforeEach
    void setUp() {
        request = new FuelGradePriceChangeRequest();
    }

    @Test
    void testSetAndGetStation() {
        Station station = new Station();
        station.setId(1L); // Assuming setter exists in Station

        request.setStation(station);
        assertEquals(station, request.getStation());
    }

    @Test
    void testSetAndGetStationId() {
        request.setStationId(2L);
        assertEquals(Long.valueOf(2), request.getStationId());
    }

    @Test
    void testSetAndGetRequestType() {
        request.setRequestType(EnumRequestType.AUTHORIZATION_REQUEST);
        assertEquals(EnumRequestType.AUTHORIZATION_REQUEST, request.getRequestType());
    }

    @Test
    void testSetAndGetScheduledDate() {
        LocalDateTime now = LocalDateTime.now();
        request.setScheduledDate(now);
        assertEquals(now, request.getScheduledDate());
    }

    @Test
    void testSetAndGetRequester() {
        User user = new User();
        user.setId(3L); // Assuming setter exists in User

        request.setRequester(user);
        assertEquals(user, request.getRequester());
    }

    @Test
    void testSetAndGetRequesterId() {
        request.setRequesterId(4L);
        assertEquals(Long.valueOf(4), request.getRequesterId());
    }

    @Test
    void testSetAndGetStatus() {
        request.setStatus(EnumRequestStatus.WAITING_RESPONSE);
        assertEquals(EnumRequestStatus.WAITING_RESPONSE, request.getStatus());
    }
}