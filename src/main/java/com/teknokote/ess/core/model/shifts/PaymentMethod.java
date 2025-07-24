package com.teknokote.ess.core.model.shifts;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.CustomerAccount;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Entity
@Getter
@Setter
public class PaymentMethod extends ESSEntity<Long, User>
{
   @Serial
   private static final long serialVersionUID = -5640679986313081704L;
   private String code;
   private String description;
   @ManyToOne
   private CustomerAccount customerAccount;
   @Column(name = "customer_account_id",insertable = false, updatable = false)
   private Long customerAccountId;
}
