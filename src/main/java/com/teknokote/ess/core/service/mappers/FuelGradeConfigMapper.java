package com.teknokote.ess.core.service.mappers;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.pts.client.response.configuration.PTSFuelGrade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
@Mapper(config = MapperConfiguration.class)
public interface FuelGradeConfigMapper {
    @Mapping(target = "id", source = "idConf")
    PTSFuelGrade toPTSFuelGrade(FuelGradeConfigDto fuelGradeConfigDto);

    List<PTSFuelGrade> toPtsFuelGradeList(List<FuelGradeConfigDto> fuelGrades);

    FuelGradeConfigDto toDtoFuelGrade(FuelGrade fuelGrade);

    List<FuelGradeConfigDto> toDtoFuelGradeList(List<FuelGrade> fuelGrades);
    List<FuelGradeConfigDto> toDtoPTSFuelGrade(List<PTSFuelGrade> fuelGrades);

}
