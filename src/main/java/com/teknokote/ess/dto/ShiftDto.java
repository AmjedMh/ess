package com.teknokote.ess.dto;

import com.teknokote.core.dto.ESSIdentifiedDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class ShiftDto extends ESSIdentifiedDto<Long>{
    private static final String OFF_DAY = "Off day"; // Jour de repos
    private int index;
    private String name;
    private Long shiftRotationId;
    private LocalTime startingTime;
    private LocalTime endingTime;
    private Boolean offDay;
    private boolean crossesDayBoundary;
    @Builder
    public ShiftDto(Long id,Long version,int index,String name,LocalTime startingTime,LocalTime endingTime,Long shiftRotationId,Boolean offDay,boolean crossesDayBoundary)
    {
        super(id,version);
        this.index=index;
        this.name=name;
        this.shiftRotationId=shiftRotationId;
        this.startingTime=startingTime;
        this.endingTime=endingTime;
        this.offDay=offDay;
        this.crossesDayBoundary=crossesDayBoundary;
    }

    public static ShiftDto createOffDay()
    {
        return ShiftDto.builder().name(OFF_DAY).offDay(true).build();
    }
}
