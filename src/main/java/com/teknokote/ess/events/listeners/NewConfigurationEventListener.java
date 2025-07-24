package com.teknokote.ess.events.listeners;

import com.teknokote.ess.core.dao.CustomerAccountDao;
import com.teknokote.ess.core.service.mappers.FuelGradeConfigMapper;
import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.events.publish.CustomerAccountExport;
import com.teknokote.ess.events.publish.cm.exception.ExceptionHandlerUtil;
import com.teknokote.ess.events.types.NewConfigurationUploadedEvent;
import com.teknokote.pts.client.response.ResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSFuelGrade;
import com.teknokote.pts.client.response.configuration.PTSFuelGradesConfigurationResponsePacket;
import com.teknokote.pts.client.upload.configuration.UploadConfigRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
@Setter
public class NewConfigurationEventListener {
    @Autowired
    private CustomerAccountDao customerAccountDao;
    @Autowired
    private FuelGradeConfigMapper fuelGradeConfigMapper;
    @Autowired
    private CustomerAccountExport customerAccountExport;

    @EventListener
    @Transactional
    public void handleNewConfigurationUpload(NewConfigurationUploadedEvent newConfigurationUploadedEvent) {
        final List<PTSFuelGrade> fuelGradesFromRequest = getFuelGradesFromRequest(newConfigurationUploadedEvent.getUploadConfigRequest());

        CustomerAccountDto customerAccount = customerAccountDao.findByStationControllerPtsId(newConfigurationUploadedEvent.getPtsId());
            if (customerAccount != null && customerAccount.isExported()) {
                exportProduct(customerAccount, fuelGradesFromRequest);
        }
    }

    private List<PTSFuelGrade> getFuelGradesFromRequest(UploadConfigRequest request) {
        List<PTSFuelGrade> fuelGrades = new ArrayList<>();

        for (ResponsePacket packet : request.getPackets().get(0).getConfiguration()) {
            if (packet instanceof PTSFuelGradesConfigurationResponsePacket fuelGradesPacket) {
                fuelGradesPacket = (PTSFuelGradesConfigurationResponsePacket) packet;
                fuelGrades.addAll(fuelGradesPacket.getData().getFuelGrades());
            }
        }
        return fuelGrades;
    }

    private void exportProduct(CustomerAccountDto customerAccountDto, List<PTSFuelGrade> fuelGradeList) {
        if (!fuelGradeList.isEmpty()) {
            List<FuelGradeConfigDto> fuelGrades = fuelGradeConfigMapper.toDtoPTSFuelGrade(fuelGradeList);
            for (FuelGradeConfigDto fuelGrade : fuelGrades) {
                fuelGrade.setReference(String.valueOf(String.valueOf(customerAccountDto.getId())));
                try {
                    customerAccountExport.productCreated(fuelGrade);
                }catch (Exception e) {
                    ExceptionHandlerUtil.handleException(e, "Card Manager");
                }
            }
        }
    }
}
