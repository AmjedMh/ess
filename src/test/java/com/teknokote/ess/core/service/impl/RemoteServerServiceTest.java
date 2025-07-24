package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.repository.RemoteServerRepository;
import com.teknokote.pts.client.response.JsonPTSResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RemoteServerServiceTest {

    @InjectMocks
    private RemoteServerService remoteServerService;
    @Mock
    private RemoteServerRepository remoteServerRepository;
    private ControllerPtsConfiguration controllerPtsConfiguration;
    private JsonPTSResponse jsonPTSResponse;

    @BeforeEach
    void setUp() {
        // Initialize the mock objects
        controllerPtsConfiguration = mock(ControllerPtsConfiguration.class);
        jsonPTSResponse = mock(JsonPTSResponse.class);
    }

    @Test
    void addNewRemote_ShouldReturnResponse_WhenCalledWithValidParams() {
        // When
        JsonPTSResponse result = remoteServerService.addNewRemote(controllerPtsConfiguration, jsonPTSResponse);

        // Then
        assertNotNull(result);
        assertEquals(jsonPTSResponse, result);
    }

}