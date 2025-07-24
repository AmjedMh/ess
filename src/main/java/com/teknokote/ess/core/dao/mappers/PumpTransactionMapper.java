package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.core.service.impl.NozzleService;
import com.teknokote.ess.core.service.impl.PumpService;
import com.teknokote.pts.client.upload.pump.PumpUpload;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfiguration.class)
public interface PumpTransactionMapper
{
   @Mapping(target="nozzle", expression = "java(nozzleService.findNozzleByIdConfAndPump(pumpUpload.getNozzle(),pumpUpload.getPump(),pumpUpload.getConfigurationId(),ptsId))")
   @Mapping(target = "pump", expression ="java(pumpService.findByIdConfAndConfigurationIdAndControllerId(ptsId,pumpUpload.getConfigurationId(),pumpUpload.getPump()))" )
   PumpTransaction fromPumpUploadedData(PumpUpload pumpUpload, String ptsId, @Context PumpService pumpService, @Context NozzleService nozzleService);

   @Mapping(target = "nozzle",ignore = true)
   @Mapping(target = "pump",ignore = true)
   @Mapping(target = "state", expression ="java(com.teknokote.ess.core.model.movements.EnumTransactionState.FINISHED)" )
   PumpTransaction updateAndMarkAsFinished(PumpUpload pumpUpload, @MappingTarget PumpTransaction pumpTransaction);
}
