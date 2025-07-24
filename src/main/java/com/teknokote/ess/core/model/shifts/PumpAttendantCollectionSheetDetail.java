package com.teknokote.ess.core.model.shifts;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class PumpAttendantCollectionSheetDetail extends ESSEntity<Long, User>
{
   @Serial
   private static final long serialVersionUID = -6179353234701789563L;

   @ManyToOne
   private PumpAttendantCollectionSheet pumpAttendantCollectionSheet;
   @Column(name = "pump_attendant_collection_sheet_id",insertable = false, updatable = false)
   private Long pumpAttendantCollectionSheetId;
   @ManyToOne
   private PaymentMethod paymentMethod;
   @Column(name = "payment_method_id",insertable = false, updatable = false)
   private Long paymentMethodId;
   private BigDecimal amount;
}
