package com.teknokote.ess.dto.shifts;

import com.teknokote.core.dto.ESSIdentifiedDto;
import com.teknokote.ess.core.model.shifts.EnumPumpAttendantSheetStatus;
import com.teknokote.ess.core.model.shifts.ShiftExecutionStatus;
import com.teknokote.ess.dto.ShiftDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Getter
@Setter
public class ShiftPlanningExecutionDto extends ESSIdentifiedDto<Long>
{
   private Integer index;
   private ShiftDto shift;
   private Long shiftId;
   private Long shiftPlanningId;
   private Long workDayShiftPlanningExecutionId;
   private LocalDateTime startDateTime;
   private LocalDateTime endDateTime;
   private ShiftExecutionStatus status;
   private List<ShiftPlanningExecutionDetailDto> shiftPlanningExecutionDetail;
   private List<PumpAttendantCollectionSheetDto> pumpAttendantCollectionSheets;


   @Builder
   public ShiftPlanningExecutionDto(Long id,Long version,Integer index, ShiftDto shift, Long shiftId, Long workDayShiftPlanningExecutionId, LocalDateTime startDateTime, LocalDateTime endDateTime, Long shiftPlanningId, ShiftExecutionStatus status, List<ShiftPlanningExecutionDetailDto> shiftPlanningExecutionDetail,List<PumpAttendantCollectionSheetDto> pumpAttendantCollectionSheets)
   {
      super(id,version);
      this.index=index;
      this.shift = shift;
      this.shiftId = shiftId;
      this.workDayShiftPlanningExecutionId = workDayShiftPlanningExecutionId;
      this.startDateTime = startDateTime;
      this.endDateTime = endDateTime;
      this.shiftPlanningId = shiftPlanningId;
      this.status = status;
      this.pumpAttendantCollectionSheets=pumpAttendantCollectionSheets;
      this.shiftPlanningExecutionDetail = new ArrayList<>();
      this.shiftPlanningExecutionDetail.addAll(shiftPlanningExecutionDetail);
   }

   /**
    * Collecte les feuilles de recette des pompistes
    * @param pumpAttendantSheetMap
    */
   public void createPumpAttendantSheets(Map<Long, BigDecimal> pumpAttendantSheetMap)
   {
      for (Map.Entry<Long, BigDecimal> entry : pumpAttendantSheetMap.entrySet()) {
         Long pumpAttendantId = entry.getKey();
         BigDecimal totalAmount = entry.getValue();

         PumpAttendantCollectionSheetDto pumpAttendantCollectionSheet = PumpAttendantCollectionSheetDto.builder()
            .shiftPlanningExecutionId(this.getId())
            .pumpAttendantId(pumpAttendantId)
            .status(EnumPumpAttendantSheetStatus.INITIATED)
            .totalAmount(totalAmount)
            .build();

         this.addPumpAttendantSheet(pumpAttendantCollectionSheet);
      }
   }

   private void addPumpAttendantSheet(PumpAttendantCollectionSheetDto pumpAttendantCollectionSheet){
      this.pumpAttendantCollectionSheets.add(pumpAttendantCollectionSheet);
   }

   public void stopExecution(LocalDateTime realEndDateTime)
   {
      this.status=ShiftExecutionStatus.COMPLETED;
      this.endDateTime=realEndDateTime;
   }

   public void start(LocalDateTime realStartDateTime)
   {
      this.setStartDateTime(realStartDateTime);
      this.setStatus(ShiftExecutionStatus.IN_PROGRESS);
   }

   public boolean isWaiting(){
      return ShiftExecutionStatus.WAITING.equals(getStatus());
   }
   public boolean isInProgress(){
      return ShiftExecutionStatus.IN_PROGRESS.equals(getStatus());
   }
   public boolean isCompleted(){
      return ShiftExecutionStatus.COMPLETED.equals(getStatus());
   }
   public boolean isLocked(){
      return ShiftExecutionStatus.LOCKED.equals(getStatus());
   }
   public boolean isNotWaiting(){
      return !isWaiting();
   }
   public boolean isNotInProgress(){
      return !isInProgress();
   }
   public boolean isNotCompleted(){
      return !isCompleted();
   }
   public boolean isNotLocked(){
      return !isLocked();
   }

   public void markCompleted(){
      this.setStatus(ShiftExecutionStatus.COMPLETED);
   }
   public void markWaiting(){
      this.setStatus(ShiftExecutionStatus.WAITING);
   }
   public void markInProgress(){
      this.setStatus(ShiftExecutionStatus.IN_PROGRESS);
   }
   public void markLocked(){
      this.setStatus(ShiftExecutionStatus.LOCKED);
   }


   public void addShiftPlanningExecutionDetails(List<ShiftPlanningExecutionDetailDto> shiftPlanningExecutionDetailsToAdd) {
      Optional<ShiftPlanningExecutionDetailDto> maxIndexElement = shiftPlanningExecutionDetail.stream()
              .max(Comparator.comparing(ShiftPlanningExecutionDetailDto::getIndex));

      final AtomicInteger maxIndex = new AtomicInteger(maxIndexElement.map(ShiftPlanningExecutionDetailDto::getIndex).orElse(0));

      shiftPlanningExecutionDetailsToAdd.forEach(el -> {
         el.setIndex(maxIndex.incrementAndGet());
         shiftPlanningExecutionDetail.add(el);
      });
   }

   /**
    * Renvoi un stream de ShiftPlanningExecutionDetailDto du pompiste choisi
    * @param pumpAttendantId
    * @return
    */
   public Stream<ShiftPlanningExecutionDetailDto> getPumpAttendantShiftExecutionDetails(Long pumpAttendantId)
   {
      return this.getShiftPlanningExecutionDetail()
         .stream().filter(el -> el.getPumpAttendantId().equals(pumpAttendantId));
   }
}
