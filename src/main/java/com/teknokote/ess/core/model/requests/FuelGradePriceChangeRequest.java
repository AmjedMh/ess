package com.teknokote.ess.core.model.requests;

import com.teknokote.ess.core.model.configuration.FuelGrade;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Entity
@Getter
@Setter
@DiscriminatorValue("FUEL_GRADE_PRICE_CHANGE")
public class FuelGradePriceChangeRequest extends AbstractRequest
{
   @Serial
   private static final long serialVersionUID = 9115634246792441178L;
   private Double oldPrice;
   private Double newPrice; // Planned price
   @ManyToOne
   private FuelGrade fuelGrade;
   @Column(name = "fuel_Grade_id",insertable = false, updatable = false)
   private Long fuelGradeId;
}
