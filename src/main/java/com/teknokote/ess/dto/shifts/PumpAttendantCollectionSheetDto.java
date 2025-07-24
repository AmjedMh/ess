package com.teknokote.ess.dto.shifts;

import com.teknokote.ess.core.model.shifts.EnumPumpAttendantSheetStatus;
import com.teknokote.core.dto.ESSIdentifiedDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
public class PumpAttendantCollectionSheetDto extends ESSIdentifiedDto<Long>{
    private Long shiftPlanningExecutionId;
    private Long pumpAttendantId;
        private EnumPumpAttendantSheetStatus status;
    private BigDecimal totalAmount;
    private String inconsistencyMotif;
    private Set<PumpAttendantCollectionSheetDetailDto> collectionSheetDetails;
    @Builder
    public PumpAttendantCollectionSheetDto(Long id,Long version,Long shiftPlanningExecutionId,Long pumpAttendantId,EnumPumpAttendantSheetStatus status,BigDecimal totalAmount,Set<PumpAttendantCollectionSheetDetailDto> collectionSheetDetails,String inconsistencyMotif)
    {
        super(id,version);
        this.shiftPlanningExecutionId = shiftPlanningExecutionId;
        this.pumpAttendantId = pumpAttendantId;
        this.collectionSheetDetails = collectionSheetDetails;
        this.totalAmount=totalAmount;
        this.status=status;
        this.inconsistencyMotif=inconsistencyMotif;
    }

    public boolean isTreated(){
        return status.ordinal()>EnumPumpAttendantSheetStatus.INITIATED.ordinal();
    }
}
