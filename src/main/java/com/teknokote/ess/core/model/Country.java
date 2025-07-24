package com.teknokote.ess.core.model;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Country extends ESSEntity<Long, User>
{

   @Serial
   private static final long serialVersionUID = 5151560098018139657L;
   private String code;
   private String name;
   @ManyToOne
   private Currency currency;
}
