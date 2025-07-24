package com.teknokote.ess.core.model.shifts;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class ShiftRotation extends ESSEntity<Long, User>
{
   @Serial
   private static final long serialVersionUID = -6066064401143653919L;
   private String name;
   private LocalDate startValidityDate;
   private LocalDate endValidityDate;
   private Integer nbrOffDays;
   @Enumerated(EnumType.STRING)
   private EnumPlanificationMode planificationMode;

   @ManyToOne
   private Station station;
   @Column(name = "station_id",insertable = false, updatable = false)
   private Long stationId;
   @OneToMany(mappedBy = "shiftRotation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
   @OrderColumn(name = "index")
   private List<Shift> shifts;
}
