package com.teknokote.ess.core.dao.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.core.mappers.BidirectionnelMapper;
import com.teknokote.ess.core.model.FirmwareInformation;
import com.teknokote.ess.dto.FirmwareInformationDto;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface FirmwareInformationMapper extends BidirectionnelMapper<FirmwareInformation, FirmwareInformationDto>
{
}
