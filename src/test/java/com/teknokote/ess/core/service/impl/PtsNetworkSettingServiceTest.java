package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.PtsNetwork;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.repository.PtsNetworkSettingRepository;
import com.teknokote.pts.client.response.JsonPTSResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PtsNetworkSettingServiceTest {

    @InjectMocks
    private PtsNetworkSettingService ptsNetworkSettingService;
    @Mock
    private PtsNetworkSettingRepository ptsNetworkSettingRepository;
    private List<PtsNetwork> ptsNetworkList;
    @BeforeEach
    void setUp() {
        ptsNetworkList = new ArrayList<>();

        PtsNetwork network1 = new PtsNetwork();
        network1.setId(1L);

        PtsNetwork network2 = new PtsNetwork();
        network2.setId(2L);

        ptsNetworkList.add(network1);
        ptsNetworkList.add(network2);
    }

    @Test
    void getAll_ShouldReturnAllNetworks_WhenCalled() {
        // Given
        when(ptsNetworkSettingRepository.findAll()).thenReturn(ptsNetworkList);

        // When
        List<PtsNetwork> result = ptsNetworkSettingService.getAll();

        // Then
        assertEquals(2, result.size());
        assertEquals(ptsNetworkList, result);
        verify(ptsNetworkSettingRepository, times(1)).findAll();
    }

    @Test
    void addNewNetwork_ShouldReturnJsonPTSResponse_WhenCalled() {
        // Given
        ControllerPtsConfiguration controllerPtsConfiguration = new ControllerPtsConfiguration();
        controllerPtsConfiguration.setConfigurationId("4537fjys");

        JsonPTSResponse response = new JsonPTSResponse();

        // When
        JsonPTSResponse result = ptsNetworkSettingService.addNewNetwork(controllerPtsConfiguration, response);

        // Then
        assertEquals(response, result);
    }
}