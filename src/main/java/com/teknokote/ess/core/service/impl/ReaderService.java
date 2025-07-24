package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Reader;
import com.teknokote.ess.core.service.base.EntityService;
import com.teknokote.ess.dto.ReaderDto;
import com.teknokote.pts.client.response.JsonPTSResponse;

import java.util.List;

public interface ReaderService extends EntityService<Reader, Long>
{
    JsonPTSResponse addNewReader(ControllerPtsConfiguration controllerPtsConfiguration, JsonPTSResponse response);

    ReaderDto mapToReaderConfigDto(Reader reader);
    public List<Reader> findReadersByControllerOnCurrentConfiguration(Long idCtr);

}
